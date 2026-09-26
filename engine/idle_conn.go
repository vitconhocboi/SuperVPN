package engine

import (
	"net"
	"time"
)

// idleConn pushes the read/write deadline forward on every I/O call, so a
// connection dies only after `timeout` without progress in that direction.
// Unlike a fixed deadline it never cuts off a large but steady transfer.
type idleConn struct {
	net.Conn
	timeout time.Duration
}

func withIdleTimeout(c net.Conn, timeout time.Duration) net.Conn {
	return &idleConn{Conn: c, timeout: timeout}
}

func (c *idleConn) Read(p []byte) (int, error) {
	_ = c.Conn.SetReadDeadline(time.Now().Add(c.timeout))
	return c.Conn.Read(p)
}

func (c *idleConn) Write(p []byte) (int, error) {
	_ = c.Conn.SetWriteDeadline(time.Now().Add(c.timeout))
	return c.Conn.Write(p)
}
