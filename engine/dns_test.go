package engine

import (
	"encoding/hex"
	"testing"
)

func TestParseDNSQueryAndNXDomain(t *testing.T) {
	// Sample DNS query for ads.example.com
	// TxID: 0x1234, Flags: 0x0100 (Standard query), QDCOUNT: 1
	// Question: ads (3) example (7) com (3) 0, Type A (1), Class IN (1)
	queryHex := "12340100000100000000000003616473076578616d706c6503636f6d0000010001"

	query, err := hex.DecodeString(queryHex)
	if err != nil {
		t.Fatalf("failed to decode hex: %v", err)
	}

	txID, domain, qtype, err := ParseDNSQuery(query)
	if err != nil {
		t.Fatalf("ParseDNSQuery failed: %v", err)
	}

	if txID != 0x1234 {
		t.Errorf("got TxID 0x%x, want 0x1234", txID)
	}
	if domain != "ads.example.com" {
		t.Errorf("got domain %q, want %q", domain, "ads.example.com")
	}
	if qtype != 1 {
		t.Errorf("got qtype %d, want 1", qtype)
	}

	nxResp := BuildNXDomainResponse(query)
	if nxResp == nil {
		t.Fatalf("BuildNXDomainResponse returned nil")
	}

	if nxResp[2] != 0x81 || nxResp[3] != 0x83 {
		t.Errorf("got flags 0x%02x%02x, want 0x8183", nxResp[2], nxResp[3])
	}
}
