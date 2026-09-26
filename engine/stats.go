package engine

import "sync/atomic"

var globalStats EngineStats

// Stats returns a snapshot of engine statistics.
func Stats() *EngineStats {
	return &EngineStats{
		DNSBlocked:   atomic.LoadInt64(&globalStats.DNSBlocked),
		TCPBlocked:   atomic.LoadInt64(&globalStats.TCPBlocked),
		QUICBlocked:  atomic.LoadInt64(&globalStats.QUICBlocked),
		MITMBlocked:  atomic.LoadInt64(&globalStats.MITMBlocked),
		Forwarded:    atomic.LoadInt64(&globalStats.Forwarded),
		ForwardedTCP: atomic.LoadInt64(&globalStats.ForwardedTCP),
		ForwardedUDP: atomic.LoadInt64(&globalStats.ForwardedUDP),
		ActiveConns:  atomic.LoadInt64(&globalStats.ActiveConns),
		Errored:      atomic.LoadInt64(&globalStats.Errored),
	}
}
