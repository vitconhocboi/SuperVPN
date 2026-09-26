//go:build windows

package engine

func dupFD(fd int) (int, error) {
	return fd, nil
}

func closeFD(fd int) {
}
