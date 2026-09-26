//go:build !windows

package engine

import "golang.org/x/sys/unix"

func dupFD(fd int) (int, error) {
	return unix.Dup(fd)
}

func closeFD(fd int) {
	_ = unix.Close(fd)
}
