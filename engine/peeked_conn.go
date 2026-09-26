package engine

import "net"

// peekedConn replays bytes already consumed while sniffing (e.g. the TLS
// ClientHello) before reading from the underlying conn, so a consumer such as
// tls.Server sees the byte stream exactly as the client sent it.
type peekedConn struct {
	net.Conn
	peeked []byte
}

func (c *peekedConn) Read(p []byte) (int, error) {
	if len(c.peeked) > 0 {
		n := copy(p, c.peeked)
		c.peeked = c.peeked[n:]
		return n, nil
	}
	return c.Conn.Read(p)
}
