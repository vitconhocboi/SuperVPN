package engine

import (
	"encoding/hex"
	"testing"
)

func TestExtractSNI(t *testing.T) {
	// Sample TLS ClientHello payload with SNI "example.com"
	clientHelloHex := "16030100c6010000c20303" +
		"0011223344556677889900112233445566778899001122334455667788990011" + // 32 byte random
		"00" + // session id len 0
		"0002002f" + // cipher suites
		"0100" + // compression
		"0097" + // extensions len
		"00000010000e00000b6578616d706c652e636f6d" // SNI extension: "example.com"

	data, err := hex.DecodeString(clientHelloHex)
	if err != nil {
		t.Fatalf("failed to decode hex: %v", err)
	}

	sni, err := ExtractSNI(data)
	if err != nil {
		t.Fatalf("ExtractSNI failed: %v", err)
	}

	if sni != "example.com" {
		t.Errorf("got SNI %q, want %q", sni, "example.com")
	}
}

func TestExtractSNI_NonTLS(t *testing.T) {
	data := []byte("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n")
	_, err := ExtractSNI(data)
	if err == nil {
		t.Errorf("expected error for non-TLS data, got nil")
	}
}
