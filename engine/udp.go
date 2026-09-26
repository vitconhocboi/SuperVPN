package engine

import (
	"net"
	"strconv"
	"strings"
	"sync/atomic"
	"time"

	"github.com/xjasonlyu/tun2socks/v2/core/adapter"
)

const udpIdleTimeout = 60 * time.Second

func handleUDPFlow(conn adapter.UDPConn) {
	defer conn.Close()

	atomic.AddInt64(&globalStats.ActiveConns, 1)
	defer atomic.AddInt64(&globalStats.ActiveConns, -1)

	id := conn.ID()
	dstPort := int(id.LocalPort)
	targetAddr := net.JoinHostPort(id.LocalAddress.String(), strconv.Itoa(dstPort))

	bl := getBlocklist()

	if dstPort == 53 {
		handleDNSFlow(conn, targetAddr, bl)
		return
	}

	_ = conn.SetReadDeadline(time.Now().Add(5 * time.Second))
	buf := make([]byte, 65535)
	n, err := conn.Read(buf)
	_ = conn.SetReadDeadline(time.Time{})
	if err != nil || n <= 0 {
		return
	}

	firstPkt := buf[:n]

	if dstPort == 443 && bl != nil {
		sni, err := ExtractQUICSNI(firstPkt)
		if err == nil && sni != "" {
			res := bl.MatchDomain(sni)
			if res.IsBlockedDomain {
				atomic.AddInt64(&globalStats.QUICBlocked, 1)
				logger.Printf("[QUIC BLOCK] %s (target %s)", sni, targetAddr)
				return
			}
			if shouldMITM(sni, res) {
				// Path rules need TCP: dropping the QUIC Initial makes the
				// client fall back to TLS over TCP, where MITM applies.
				logger.Printf("[QUIC PIN TCP] dropping QUIC for %s so path rules apply over TCP", sni)
				return
			}
		}
	}

	remote, err := net.Dial("udp", targetAddr)
	if err != nil {
		atomic.AddInt64(&globalStats.Errored, 1)
		logger.Printf("[UDP ERR] dial %s: %v", targetAddr, err)
		return
	}
	defer remote.Close()

	atomic.AddInt64(&globalStats.Forwarded, 1)
	atomic.AddInt64(&globalStats.ForwardedUDP, 1)

	if _, err := remote.Write(firstPkt); err != nil {
		return
	}

	done := make(chan struct{}, 2)

	go func() {
		b := make([]byte, 65535)
		for {
			_ = conn.SetReadDeadline(time.Now().Add(udpIdleTimeout))
			nr, err := conn.Read(b)
			if err != nil || nr <= 0 {
				break
			}
			if _, err := remote.Write(b[:nr]); err != nil {
				break
			}
		}
		done <- struct{}{}
	}()

	go func() {
		b := make([]byte, 65535)
		for {
			_ = remote.SetReadDeadline(time.Now().Add(udpIdleTimeout))
			nr, err := remote.Read(b)
			if err != nil || nr <= 0 {
				break
			}
			if _, err := conn.Write(b[:nr]); err != nil {
				break
			}
		}
		done <- struct{}{}
	}()

	<-done
}

func handleDNSFlow(conn adapter.UDPConn, targetAddr string, bl *Blocklist) {
	_ = conn.SetReadDeadline(time.Now().Add(5 * time.Second))
	buf := make([]byte, 4096)
	n, err := conn.Read(buf)
	_ = conn.SetReadDeadline(time.Time{})
	if err != nil || n <= 0 {
		return
	}

	pkt := buf[:n]
	txID, domain, _, parseErr := ParseDNSQuery(pkt)

	if parseErr == nil && domain != "" && bl != nil {
		res := bl.MatchDomain(domain)
		if res.IsBlockedDomain {
			atomic.AddInt64(&globalStats.DNSBlocked, 1)
			logger.Printf("[DNS BLOCK] %s (txID: 0x%x)", domain, txID)
			nxResp := BuildNXDomainResponse(pkt)
			if nxResp != nil {
				_, _ = conn.Write(nxResp)
			}
			return
		}
	}

	upstream := targetAddr
	e := current
	e.mu.Lock()
	if e.key != nil && e.key.DNSServers != "" {
		servers := strings.Split(e.key.DNSServers, ",")
		if len(servers) > 0 && strings.TrimSpace(servers[0]) != "" {
			upstream = net.JoinHostPort(strings.TrimSpace(servers[0]), "53")
		}
	}
	e.mu.Unlock()

	remote, err := net.Dial("udp", upstream)
	if err != nil {
		atomic.AddInt64(&globalStats.Errored, 1)
		logger.Printf("[DNS ERR] dial upstream %s: %v", upstream, err)
		return
	}
	defer remote.Close()

	atomic.AddInt64(&globalStats.Forwarded, 1)
	atomic.AddInt64(&globalStats.ForwardedUDP, 1)

	if _, err := remote.Write(pkt); err != nil {
		return
	}

	_ = remote.SetReadDeadline(time.Now().Add(5 * time.Second))
	respBuf := make([]byte, 4096)
	rn, err := remote.Read(respBuf)
	if err == nil && rn > 0 {
		_, _ = conn.Write(respBuf[:rn])
	}
}
