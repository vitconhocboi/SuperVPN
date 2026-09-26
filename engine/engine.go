// Package engine is the in-process VPN data path (gVisor netstack), bound for
// Android via gomobile. The exported surface mirrors the old tun2socks
// `engine` package so LocalVpnService integration stays a swap, not a rewrite.
package engine

import (
	"errors"
	"fmt"
	"log"
	"os"
	"strconv"
	"sync"
	"time"

	"gvisor.dev/gvisor/pkg/tcpip/stack"

	"github.com/xjasonlyu/tun2socks/v2/core"
	"github.com/xjasonlyu/tun2socks/v2/core/adapter"
	"github.com/xjasonlyu/tun2socks/v2/core/device"
	"github.com/xjasonlyu/tun2socks/v2/core/device/fdbased"
)

const (
	// defaultMTU matches fdbased's fallback when Key.MTU is unset/invalid.
	defaultMTU = 1500
	// stopWaitTimeout bounds how long Stop waits for handler goroutines.
	stopWaitTimeout = 3 * time.Second
)

var logger = log.New(os.Stderr, "[engine] ", log.LstdFlags)

// Key carries everything needed to bring the data path up, in one shot —
// the gomobile boundary makes incremental configuration calls awkward.
type Key struct {
	MTU           int64
	FD            int32 // tun fd owned by VpnService; Go dups it at Start
	LogLevel      string
	DNSServers    string // comma-separated, mirrors Builder.addDnsServer
	BlocklistPath string // compiled blocklist file (Trie source)
	CACertPEM     string // MITM root CA (see GenerateCA); empty disables MITM
	CAKeyPEM      string
}

// EngineStats is a point-in-time snapshot of blocking decisions, split by
// the layer that made them. Used by the P3 stats screen and log viewer.
type EngineStats struct {
	DNSBlocked   int64
	TCPBlocked   int64
	QUICBlocked  int64
	MITMBlocked  int64 // requests blocked by a path rule inside MITM
	Forwarded    int64 // all flows relayed (TCP + UDP)
	ForwardedTCP int64
	ForwardedUDP int64
	ActiveConns  int64 // flows currently being handled
	Errored      int64
}

// engine holds the mutable lifecycle state; the exported API is package
// level (gomobile-friendly) and operates on this singleton.
type engine struct {
	mu sync.Mutex

	key     *Key
	dev     device.Device
	stk     *stack.Stack
	started bool

	// run is the per-Start generation: handler goroutines Add/Done on its
	// WaitGroup, Stop waits on it. A fresh one each Start keeps WaitGroup
	// reuse strictly correct across restart cycles.
	run *runContext
}

// runContext groups everything tied to one Start..Stop cycle.
type runContext struct {
	wg sync.WaitGroup
}

var current = &engine{}

// Insert queues the configuration for the next Start, mirroring the old
// tun2socks API shape (insert then start).
func Insert(key *Key) {
	e := current
	e.mu.Lock()
	e.key = key
	e.mu.Unlock()
}

// Start brings the netstack up on a dup of the tun fd (the Kotlin side owns
// the original and may close its copy at any time).
func Start() error {
	e := current

	e.mu.Lock()
	defer e.mu.Unlock()

	if e.started {
		return errors.New("engine: already started")
	}
	if e.key == nil {
		return errors.New("engine: no key inserted")
	}
	k := e.key

	mtu := uint32(k.MTU)
	if mtu <= 0 || mtu > 65535 {
		mtu = defaultMTU
	}

	dupFD, err := dupFD(int(k.FD))
	if err != nil {
		return fmt.Errorf("engine: dup tun fd %d: %w", k.FD, err)
	}

	dev, err := fdbased.Open(strconv.Itoa(dupFD), mtu, 0)
	if err != nil {
		closeFD(dupFD)
		return fmt.Errorf("engine: open fdbased device: %w", err)
	}

	stk, err := core.CreateStack(&core.Config{
		LinkEndpoint:     dev,
		TransportHandler: e,
	})
	if err != nil {
		dev.Close()
		return fmt.Errorf("engine: create netstack: %w", err)
	}

	e.dev = dev
	e.stk = stk
	e.run = &runContext{}
	e.started = true

	if err := loadCA(k.CACertPEM, k.CAKeyPEM); err != nil {
		logger.Printf("MITM CA rejected, path rules disabled: %v", err)
	}

	if k.BlocklistPath != "" {
		if err := ReloadBlocklist(k.BlocklistPath); err != nil {
			logger.Printf("initial blocklist load warning: %v", err)
		}
	}

	logger.Printf("started: tun fd=%d mtu=%d", dupFD, mtu)
	return nil
}

// Stop tears the data path down: closes the device and the stack, then waits
// (bounded) for handler goroutines spawned during this run.
func Stop() {
	e := current

	e.mu.Lock()
	dev, stk, run := e.dev, e.stk, e.run
	e.dev, e.stk, e.run = nil, nil, nil
	e.started = false
	e.mu.Unlock()

	if dev == nil {
		return
	}

	// Order matters: device close stops the link endpoint (and closes our
	// dup'ed fd), stack close aborts every live gVisor endpoint so blocked
	// handler reads/writes error out instead of lingering.
	dev.Close()
	if stk != nil {
		stk.Close()
	}

	if run != nil {
		done := make(chan struct{})
		go func() {
			run.wg.Wait()
			close(done)
		}()
		select {
		case <-done:
		case <-time.After(stopWaitTimeout):
			logger.Printf("stop: handler goroutines still running after %s", stopWaitTimeout)
		}
	}

	if stk != nil {
		stk.Wait()
	}
	logger.Printf("stopped")
}

// ReloadBlocklist atomically swaps the domain Trie.
func ReloadBlocklist(path string) error {
	bl, err := LoadBlocklistFromFile(path)
	if err != nil {
		return fmt.Errorf("engine: load blocklist %s: %w", path, err)
	}
	setBlocklist(bl)
	logger.Printf("reload blocklist success from %s", path)
	return nil
}

// HandleTCP satisfies adapter.TransportHandler. Called synchronously from
// the gVisor forwarder, so the flow work happens on its own goroutine.
func (e *engine) HandleTCP(conn adapter.TCPConn) {
	r, ok := e.beginFlow(conn)
	if !ok {
		return
	}
	r.wg.Add(1)
	go func() {
		defer r.wg.Done()
		handleTCPFlow(conn)
	}()
}

// HandleUDP satisfies adapter.TransportHandler; see HandleTCP.
func (e *engine) HandleUDP(conn adapter.UDPConn) {
	r, ok := e.beginFlow(conn)
	if !ok {
		return
	}
	r.wg.Add(1)
	go func() {
		defer r.wg.Done()
		handleUDPFlow(conn)
	}()
}

// beginFlow resolves the current run context; closes the conn and reports
// false when the engine is not running (late packets during teardown).
func (e *engine) beginFlow(conn interface{ Close() error }) (*runContext, bool) {
	e.mu.Lock()
	r, started := e.run, e.started
	e.mu.Unlock()
	if !started || r == nil {
		_ = conn.Close()
		return nil, false
	}
	return r, true
}
