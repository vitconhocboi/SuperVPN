package engine

import (
	"errors"
	"strings"
)

var errInvalidDNS = errors.New("invalid DNS packet")

// ParseDNSQuery extracts domain name and record type (A/AAAA/etc) from a UDP DNS query.
func ParseDNSQuery(data []byte) (transactionID uint16, domain string, qtype uint16, err error) {
	if len(data) < 12 {
		return 0, "", 0, errInvalidDNS
	}

	transactionID = uint16(data[0])<<8 | uint16(data[1])
	flags := uint16(data[2])<<8 | uint16(data[3])

	// Must be a query (QR bit = 0)
	if (flags & 0x8000) != 0 {
		return 0, "", 0, errInvalidDNS
	}

	qdcount := uint16(data[4])<<8 | uint16(data[5])
	if qdcount == 0 {
		return 0, "", 0, errInvalidDNS
	}

	offset := 12
	var labels []string

	for offset < len(data) {
		length := int(data[offset])
		if length == 0 {
			offset++
			break
		}
		if length&0xC0 == 0xC0 {
			return 0, "", 0, errInvalidDNS
		}
		offset++
		if offset+length > len(data) {
			return 0, "", 0, errInvalidDNS
		}
		labels = append(labels, string(data[offset:offset+length]))
		offset += length
	}

	if offset+4 > len(data) {
		return 0, "", 0, errInvalidDNS
	}

	qtype = uint16(data[offset])<<8 | uint16(data[offset+1])
	domain = strings.Join(labels, ".")
	return transactionID, domain, qtype, nil
}

// BuildNXDomainResponse creates an NXDOMAIN DNS response for the given query packet.
func BuildNXDomainResponse(query []byte) []byte {
	if len(query) < 12 {
		return nil
	}

	resp := make([]byte, len(query))
	copy(resp, query)

	// Set Response Flag (QR=1, RD=1, RA=1, RCODE=3 NXDOMAIN) -> 0x8183
	resp[2] = 0x81
	resp[3] = 0x83

	// ANCOUNT = 0, NSCOUNT = 0, ARCOUNT = 0
	resp[6] = 0x00
	resp[7] = 0x00
	resp[8] = 0x00
	resp[9] = 0x00
	resp[10] = 0x00
	resp[11] = 0x00

	return resp
}
