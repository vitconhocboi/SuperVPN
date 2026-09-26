package engine

import (
	"net"
	"strconv"
	"sync/atomic"
	"time"

	"github.com/xjasonlyu/tun2socks/v2/core/adapter"
)

func handleTCPFlow(conn adapter.TCPConn) {
	defer conn.Close()

	atomic.AddInt64(&globalStats.ActiveConns, 1)
	defer atomic.AddInt64(&globalStats.ActiveConns, -1)

	id := conn.ID()
	targetAddr := net.JoinHostPort(id.LocalAddress.String(), strconv.Itoa(int(id.LocalPort)))

	_ = conn.SetReadDeadline(time.Now().Add(3 * time.Second))
	buf := make([]byte, 4096)
	n, err := conn.Read(buf)
	_ = conn.SetReadDeadline(time.Time{})

	var peeked []byte
	if n > 0 {
		peeked = buf[:n]
	}

	bl := getBlocklist()
	if len(peeked) > 0 && bl != nil {
		sni, err := ExtractSNI(peeked)
		if err == nil && sni != "" {
			res := bl.MatchDomain(sni)
			if res.IsBlockedDomain {
				atomic.AddInt64(&globalStats.TCPBlocked, 1)
				logger.Printf("[TCP BLOCK] %s (target %s)", sni, targetAddr)
				return
			}
			if shouldMITM(sni, res) {
				logger.Printf("[TCP MITM] %s (target %s)", sni, targetAddr)
				handleSelectiveMITM(conn, sni, targetAddr, peeked, bl, getCA())
				return
			}
		}
	}

	remote, err := net.DialTimeout("tcp", targetAddr, 10*time.Second)
	if err != nil {
		atomic.AddInt64(&globalStats.Errored, 1)
		logger.Printf("[TCP ERR] dial %s: %v", targetAddr, err)
		return
	}
	defer remote.Close()

	atomic.AddInt64(&globalStats.Forwarded, 1)
	atomic.AddInt64(&globalStats.ForwardedTCP, 1)

	if len(peeked) > 0 {
		if _, err := remote.Write(peeked); err != nil {
			return
		}
	}

	pipeBidirectional(conn, conn, remote, remote)
}
