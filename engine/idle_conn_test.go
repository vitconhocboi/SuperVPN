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
	c := withIdleTimeout(a, 80*time.Millisecond)

	// Steady traffic spanning longer than the timeout keeps the conn alive.
	go func() {
		for i := 0; i < 4; i++ {
			time.Sleep(40 * time.Millisecond)
			b.Write([]byte{byte(i)})
		}
	}()
	buf := make([]byte, 1)
	for i := 0; i < 4; i++ {
		if _, err := c.Read(buf); err != nil {
			t.Fatalf("read %d under steady traffic: %v", i, err)
		}
	}

	// Silence past the timeout fails the read.
	if _, err := c.Read(buf); !errors.Is(err, os.ErrDeadlineExceeded) {
		t.Fatalf("expected deadline exceeded after idle, got %v", err)
	}
}
