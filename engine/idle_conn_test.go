package engine

import (
	"errors"
	"net"
	"os"
	"testing"
	"time"
)

func TestIdleConnTimesOutOnlyWithoutProgress(t *testing.T) {
	a, b := net.Pipe()
	defer a.Close()
	defer b.Close()
	// Wide margins (gap 50ms vs timeout 400ms): Windows timer granularity and
	// a loaded CI machine must not make steady traffic look idle.
	const writes = 12 // 12 x 50ms = 600ms of traffic, longer than the timeout
	c := withIdleTimeout(a, 400*time.Millisecond)

	// Steady traffic spanning longer than the timeout keeps the conn alive.
	go func() {
		for i := 0; i < writes; i++ {
			time.Sleep(50 * time.Millisecond)
			b.Write([]byte{byte(i)})
		}
	}()
	buf := make([]byte, 1)
	for i := 0; i < writes; i++ {
		if _, err := c.Read(buf); err != nil {
			t.Fatalf("read %d under steady traffic: %v", i, err)
		}
	}

	// Silence past the timeout fails the read.
	if _, err := c.Read(buf); !errors.Is(err, os.ErrDeadlineExceeded) {
		t.Fatalf("expected deadline exceeded after idle, got %v", err)
	}
}
