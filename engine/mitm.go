package engine

import (
	"bufio"
	"crypto/tls"
	"errors"
	"io"
	"net"
	"net/http"
	"sync/atomic"
	"time"
)

const (
	mitmHandshakeTimeout = 10 * time.Second
	// mitmIdleTimeout bounds silence per direction on both legs (keep-alive
	// gaps, stalled origins, quiet upgraded/WebSocket streams).
	mitmIdleTimeout = 5 * time.Minute
	mitmDialTimeout = 10 * time.Second
)

// handleSelectiveMITM terminates the client's TLS with a leaf signed by our
// CA, re-originates TLS to the real server, and relays HTTP/1.1 requests one
// by one, answering 403 for paths matched by a rule. Only reached for hosts
// that passed shouldMITM. The caller owns (and closes) clientConn.
func handleSelectiveMITM(clientConn net.Conn, sni, targetAddr string, peeked []byte, bl *Blocklist, ca *CA) {
	cert, err := ca.GetServerCertificate(sni)
	if err != nil {
		atomic.AddInt64(&globalStats.Errored, 1)
		logger.Printf("[MITM ERR] leaf cert for %s: %v", sni, err)
		return
	}

	// Advertise only HTTP/1.1 so clients never negotiate h2 with us.
	clientTLS := tls.Server(&peekedConn{Conn: clientConn, peeked: peeked}, &tls.Config{
		Certificates: []tls.Certificate{*cert},
		NextProtos:   []string{"http/1.1"},
		MinVersion:   tls.VersionTLS12,
	})
	defer clientTLS.Close()
	_ = clientTLS.SetDeadline(time.Now().Add(mitmHandshakeTimeout))
	if err := clientTLS.Handshake(); err != nil {
		if isPeerAlert(err) {
			markPinnedHost(sni)
			logger.Printf("[MITM PINNED] %s rejected our cert, passthrough from now on: %v", sni, err)
		} else {
			logger.Printf("[MITM ERR] handshake with client for %s: %v", sni, err)
		}
		return
	}
	_ = clientTLS.SetDeadline(time.Time{})

	// Dial upstream lazily, on the first request: clients that pin after the
	// handshake close without ever sending one, and cost no upstream socket.
	dial := func() (net.Conn, error) {
		c, err := tls.DialWithDialer(&net.Dialer{Timeout: mitmDialTimeout}, "tcp", targetAddr, &tls.Config{
			ServerName: sni,
			NextProtos: []string{"http/1.1"},
		})
		if err != nil {
			atomic.AddInt64(&globalStats.Errored, 1)
			logger.Printf("[MITM ERR] dial remote TLS %s (%s): %v", sni, targetAddr, err)
			return nil, err
		}
		atomic.AddInt64(&globalStats.Forwarded, 1)
		atomic.AddInt64(&globalStats.ForwardedTCP, 1)
		return c, nil
	}

	served, err := relayHTTP(withIdleTimeout(clientTLS, mitmIdleTimeout), dial, sni, bl)
	switch {
	case served > 0:
		clearSilentRefusals(sni)
	case errors.Is(err, io.EOF) && recordSilentRefusal(sni):
		logger.Printf("[MITM PINNED] %s keeps closing after handshake, passthrough from now on", sni)
	}
}

// relayHTTP proxies keep-alive HTTP/1.1 exchanges until either side closes,
// a path is blocked, or a protocol upgrade hands the stream over to a raw
// pipe. Returns how many requests reached a verdict and the error that ended
// the client read loop (io.EOF = client closed cleanly).
func relayHTTP(client net.Conn, dial func() (net.Conn, error), host string, bl *Blocklist) (int, error) {
	clientR := bufio.NewReader(client)
	var remote net.Conn
	var remoteR *bufio.Reader
	defer func() {
		if remote != nil {
			remote.Close()
		}
	}()

	for served := 0; ; served++ {
		req, err := http.ReadRequest(clientR)
		if err != nil {
			return served, err
		}

		path := req.URL.Path
		if path == "" {
			path = "/"
		}
		if bl.MatchPath(host, path) {
			atomic.AddInt64(&globalStats.MITMBlocked, 1)
			logger.Printf("[MITM PATH BLOCK] %s%s", host, path)
			writeBlockedResponse(client, req)
			return served + 1, nil
		}

		if remote == nil {
			c, err := dial()
			if err != nil {
				return served, err
			}
			remote = withIdleTimeout(c, mitmIdleTimeout)
			remoteR = bufio.NewReader(remote)
		}

		// req.Write injects Go's default User-Agent when the header is
		// absent; a present-but-empty key suppresses that.
		if _, ok := req.Header["User-Agent"]; !ok {
			req.Header["User-Agent"] = nil
		}
		if err := req.Write(remote); err != nil {
			return served, err
		}
		resp, err := http.ReadResponse(remoteR, req)
		if err != nil {
			return served, err
		}
		if resp.StatusCode == http.StatusSwitchingProtocols {
			// WebSocket etc.: headers go through, then bytes flow raw.
			if err := resp.Write(client); err != nil {
				return served, err
			}
			pipeBidirectional(client, clientR, remote, remoteR)
			return served + 1, nil
		}
		err = resp.Write(client)
		resp.Body.Close()
		if err != nil {
			return served, err
		}
		if req.Close || resp.Close {
			return served + 1, nil
		}
	}
}

func writeBlockedResponse(w io.Writer, req *http.Request) {
	resp := &http.Response{
		StatusCode:    http.StatusForbidden,
		ProtoMajor:    1,
		ProtoMinor:    1,
		Request:       req,
		Header:        http.Header{"Content-Type": {"text/plain"}},
		Body:          http.NoBody,
		ContentLength: 0,
		Close:         true,
	}
	_ = resp.Write(w)
}

// pipeBidirectional copies until either direction ends. Reads go through the
// given readers so bytes already buffered past the HTTP headers are kept.
func pipeBidirectional(client net.Conn, clientR io.Reader, remote net.Conn, remoteR io.Reader) {
	done := make(chan struct{}, 2)
	go func() {
		_, _ = io.Copy(remote, clientR)
		done <- struct{}{}
	}()
	go func() {
		_, _ = io.Copy(client, remoteR)
		done <- struct{}{}
	}()
	<-done
}
