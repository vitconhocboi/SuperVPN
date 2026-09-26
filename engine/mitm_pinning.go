package engine

import (
	"errors"
	"net"
	"sync"
	"sync/atomic"
)

// silentRefusalThreshold: consecutive MITM'd connections that completed the
// TLS handshake but closed without sending a single request before the host
// is treated as pinned. OkHttp's CertificatePinner (common on Android) checks
// pins *after* the handshake and just closes, so there is no alert to see.
const silentRefusalThreshold = 3

// pinnedHosts remembers hosts whose client refused interception (certificate
// pinning, or the app does not trust user CAs). They are forwarded untouched
// from then on, so a MITM attempt costs a failed connection or two instead of
// breaking the app. Survives blocklist reloads; cleared when the process dies.
var (
	pinnedHosts    sync.Map // host -> struct{}
	silentRefusals sync.Map // host -> *atomic.Int32
)

func isPinnedHost(host string) bool {
	_, ok := pinnedHosts.Load(normalizeDomain(host))
	return ok
}

func markPinnedHost(host string) {
	pinnedHosts.Store(normalizeDomain(host), struct{}{})
	silentRefusals.Delete(normalizeDomain(host))
}

// isPeerAlert reports whether the client aborted the handshake with a TLS
// alert (bad_certificate, unknown_ca, ...), i.e. it actively rejected our
// leaf — as opposed to a reset, timeout or network switch, which must not pin.
func isPeerAlert(err error) bool {
	var opErr *net.OpError
	return errors.As(err, &opErr) && opErr.Op == "remote error"
}

// recordSilentRefusal counts a handshake-then-close with zero requests and
// pins the host once the threshold is reached. Returns true when it pinned.
func recordSilentRefusal(host string) bool {
	key := normalizeDomain(host)
	v, _ := silentRefusals.LoadOrStore(key, new(atomic.Int32))
	if v.(*atomic.Int32).Add(1) >= silentRefusalThreshold {
		markPinnedHost(key)
		return true
	}
	return false
}

// clearSilentRefusals resets the count after a request went through: the
// client does accept our certificate.
func clearSilentRefusals(host string) {
	silentRefusals.Delete(normalizeDomain(host))
}

// shouldMITM is the single MITM gate shared by the TCP and QUIC paths: path
// rules exist, a CA is loaded, and the host has not refused interception.
func shouldMITM(host string, res MatchResult) bool {
	return res.NeedsMITM && getCA() != nil && !isPinnedHost(host)
}
