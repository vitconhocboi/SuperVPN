package engine

import (
	"bufio"
	"crypto/tls"
	"crypto/x509"
	"io"
	"net"
	"net/http"
	"strings"
	"testing"
	"time"
)

// fakeOrigin answers every request on conn with the request path as body,
// or upgrades /ws and then echoes raw bytes.
func fakeOrigin(t *testing.T, conn net.Conn) {
	t.Helper()
	go func() {
		defer conn.Close()
		r := bufio.NewReader(conn)
		for {
			req, err := http.ReadRequest(r)
			if err != nil {
				return
			}
			if req.URL.Path == "/ws" {
				io.WriteString(conn, "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n")
				io.Copy(conn, r)
				return
			}
			body := "origin:" + req.URL.Path + " ua=" + req.Header.Get("User-Agent")
			resp := &http.Response{StatusCode: 200, ProtoMajor: 1, ProtoMinor: 1, Request: req,
				ContentLength: int64(len(body)), Body: io.NopCloser(strings.NewReader(body)), Header: http.Header{}}
			if err := resp.Write(conn); err != nil {
				return
			}
		}
	}()
}

func doRequest(t *testing.T, conn net.Conn, r *bufio.Reader, path string) *http.Response {
	t.Helper()
	req, _ := http.NewRequest("GET", "http://ads.example.com"+path, nil)
	req.Header.Set("User-Agent", "test-agent")
	if err := req.Write(conn); err != nil {
		t.Fatalf("write %s: %v", path, err)
	}
	resp, err := http.ReadResponse(r, req)
	if err != nil {
		t.Fatalf("read %s: %v", path, err)
	}
	return resp
}

func newRelay(t *testing.T) (net.Conn, *bufio.Reader, chan struct{}) {
	bl := NewBlocklist()
	bl.AddPathRule("ads.example.com", "/banner")
	clientSide, proxyClient := net.Pipe()
	proxyRemote, originSide := net.Pipe()
	fakeOrigin(t, originSide)
	done := make(chan struct{})
	go func() {
		dial := func() (net.Conn, error) { return proxyRemote, nil }
		relayHTTP(proxyClient, dial, "ads.example.com", bl)
		proxyClient.Close()
		proxyRemote.Close()
		close(done)
	}()
	_ = clientSide.SetDeadline(time.Now().Add(5 * time.Second))
	return clientSide, bufio.NewReader(clientSide), done
}

func TestRelayHTTPForwardsKeepAliveThenBlocksPath(t *testing.T) {
	client, r, done := newRelay(t)
	defer client.Close()

	for _, p := range []string{"/content", "/about/banner?q=1"} {
		resp := doRequest(t, client, r, p)
		body, _ := io.ReadAll(resp.Body)
		if resp.StatusCode != 200 || !strings.HasPrefix(string(body), "origin:"+strings.Split(p, "?")[0]) {
			t.Fatalf("%s: got %d %q", p, resp.StatusCode, body)
		}
		if !strings.Contains(string(body), "ua=test-agent") {
			t.Errorf("User-Agent must be forwarded unchanged, got %q", body)
		}
	}
	// Rules are prefix-anchored: "/about/banner" above passed, this one does not.
	resp := doRequest(t, client, r, "/banner/1.png")
	if resp.StatusCode != http.StatusForbidden {
		t.Fatalf("expected 403 for blocked path, got %d", resp.StatusCode)
	}
	select {
	case <-done:
	case <-time.After(3 * time.Second):
		t.Fatal("relay must close the connection after a blocked path")
	}
}

func TestRelayHTTPUpgradePipesRawBytes(t *testing.T) {
	client, r, _ := newRelay(t)
	defer client.Close()

	resp := doRequest(t, client, r, "/ws")
	if resp.StatusCode != http.StatusSwitchingProtocols {
		t.Fatalf("expected 101, got %d", resp.StatusCode)
	}
	io.WriteString(client, "ping")
	buf := make([]byte, 4)
	if _, err := io.ReadFull(r, buf); err != nil || string(buf) != "ping" {
		t.Fatalf("expected raw echo after upgrade, got %q err=%v", buf, err)
	}
}

func TestMITMHandshakeRefusalMarksHostPinned(t *testing.T) {
	p, _ := GenerateCA()
	ca, _ := parseCA(p.CertPEM, p.KeyPEM)
	host := "pinned.example.com"
	pinnedHosts.Delete(host)

	appSide, proxySide := net.Pipe()
	done := make(chan struct{})
	go func() {
		handleSelectiveMITM(proxySide, host, "127.0.0.1:1", nil, NewBlocklist(), ca)
		proxySide.Close()
		close(done)
	}()
	// Client with system roots only: our CA is untrusted, like a pinning app.
	c := tls.Client(appSide, &tls.Config{ServerName: host})
	_ = c.SetDeadline(time.Now().Add(5 * time.Second))
	if err := c.Handshake(); err == nil {
		t.Fatal("handshake must fail for a client not trusting our CA")
	}
	appSide.Close()
	<-done
	if !isPinnedHost(host) {
		t.Errorf("host must be remembered as pinned after refusal")
	}
	if shouldMITM(host, MatchResult{NeedsMITM: true}) {
		t.Errorf("pinned host must not be MITM'd again")
	}
}

func TestMITMHandshakeServesLeafForSNI(t *testing.T) {
	p, _ := GenerateCA()
	ca, _ := parseCA(p.CertPEM, p.KeyPEM)
	host := "trusting.example.com"
	pinnedHosts.Delete(host)
	roots := x509.NewCertPool()
	roots.AddCert(ca.cert)

	appSide, proxySide := net.Pipe()
	go func() {
		// Remote dial to a closed port fails after the client handshake.
		handleSelectiveMITM(proxySide, host, "127.0.0.1:1", nil, NewBlocklist(), ca)
		proxySide.Close()
	}()
	c := tls.Client(appSide, &tls.Config{ServerName: host, RootCAs: roots, NextProtos: []string{"h2", "http/1.1"}})
	_ = c.SetDeadline(time.Now().Add(5 * time.Second))
	if err := c.Handshake(); err != nil {
		t.Fatalf("client trusting our CA must complete handshake: %v", err)
	}
	if got := c.ConnectionState().NegotiatedProtocol; got != "http/1.1" {
		t.Errorf("ALPN must be pinned to http/1.1, got %q", got)
	}
	appSide.Close()
	if isPinnedHost(host) {
		t.Errorf("successful handshake must not mark host pinned")
	}
}

func TestShouldMITMRequiresCA(t *testing.T) {
	_ = loadCA("", "")
	if shouldMITM("x.example.com", MatchResult{NeedsMITM: true}) {
		t.Errorf("no CA loaded → never MITM")
	}
	p, _ := GenerateCA()
	_ = loadCA(p.CertPEM, p.KeyPEM)
	defer loadCA("", "")
	if !shouldMITM("x.example.com", MatchResult{NeedsMITM: true}) {
		t.Errorf("CA loaded + path rules → MITM")
	}
	if shouldMITM("x.example.com", MatchResult{}) {
		t.Errorf("no path rules → no MITM")
	}
}

// mitmClient runs handleSelectiveMITM for host and returns the app-side conn.
func mitmClient(t *testing.T, host string) (net.Conn, *CA, chan struct{}) {
	t.Helper()
	p, _ := GenerateCA()
	ca, _ := parseCA(p.CertPEM, p.KeyPEM)
	appSide, proxySide := net.Pipe()
	done := make(chan struct{})
	go func() {
		handleSelectiveMITM(proxySide, host, "127.0.0.1:1", nil, NewBlocklist(), ca)
		proxySide.Close()
		close(done)
	}()
	return appSide, ca, done
}

func TestMITMAbortedHandshakeDoesNotPin(t *testing.T) {
	host := "flaky.example.com"
	pinnedHosts.Delete(host)
	appSide, _, done := mitmClient(t, host)
	// Network switch / reset mid-handshake: bytes stop, the conn just dies.
	appSide.Write([]byte{0x16, 0x03, 0x01})
	appSide.Close()
	<-done
	if isPinnedHost(host) {
		t.Errorf("a dropped connection must not pin the host")
	}
}

func TestMITMSilentCloseAfterHandshakePinsAtThreshold(t *testing.T) {
	host := "okhttp-pinned.example.com"
	pinnedHosts.Delete(host)
	silentRefusals.Delete(host)
	for i := 1; i <= silentRefusalThreshold; i++ {
		appSide, ca, done := mitmClient(t, host)
		roots := x509.NewCertPool()
		roots.AddCert(ca.cert)
		c := tls.Client(appSide, &tls.Config{ServerName: host, RootCAs: roots})
		_ = c.SetDeadline(time.Now().Add(5 * time.Second))
		if err := c.Handshake(); err != nil {
			t.Fatalf("handshake %d: %v", i, err)
		}
		c.Close() // pin check failed app-side: close without a request
		<-done
		if got, want := isPinnedHost(host), i == silentRefusalThreshold; got != want {
			t.Fatalf("after %d silent closes pinned=%v, want %v", i, got, want)
		}
	}
}

func TestSilentRefusalCountResetsOnServedRequest(t *testing.T) {
	host := "sometimes-idle.example.com"
	pinnedHosts.Delete(host)
	silentRefusals.Delete(host)
	for i := 0; i < silentRefusalThreshold-1; i++ {
		recordSilentRefusal(host)
	}
	clearSilentRefusals(host)
	if recordSilentRefusal(host) || isPinnedHost(host) {
		t.Errorf("count must restart after a served request")
	}
}
