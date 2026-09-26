package engine

import "errors"

var errNotTLS = errors.New("not a TLS record")
var errNoSNI = errors.New("SNI extension not found")

// ExtractSNI parses a TLS ClientHello packet/buffer and extracts the Server Name Indication (SNI).
func ExtractSNI(data []byte) (string, error) {
	if len(data) < 5 {
		return "", errNotTLS
	}

	// TLS Record Header: Type must be 0x16 (Handshake)
	if data[0] != 0x16 {
		return "", errNotTLS
	}

	recordLen := int(data[3])<<8 | int(data[4])
	if len(data) < 5+recordLen {
		recordLen = len(data) - 5
	}

	payload := data[5 : 5+recordLen]
	if len(payload) < 4 {
		return "", errNotTLS
	}

	// Handshake Type must be 0x01 (Client Hello)
	if payload[0] != 0x01 {
		return "", errNotTLS
	}

	handshakeLen := int(payload[1])<<16 | int(payload[2])<<8 | int(payload[3])
	if len(payload) < 4+handshakeLen {
		handshakeLen = len(payload) - 4
	}

	msg := payload[4 : 4+handshakeLen]
	// Skip Client Version (2 bytes) + Random (32 bytes) = 34 bytes
	if len(msg) < 34 {
		return "", errNoSNI
	}
	offset := 34

	// Session ID
	if offset >= len(msg) {
		return "", errNoSNI
	}
	sessionIDLen := int(msg[offset])
	offset += 1 + sessionIDLen

	// Cipher Suites
	if offset+2 > len(msg) {
		return "", errNoSNI
	}
	cipherSuitesLen := int(msg[offset])<<8 | int(msg[offset+1])
	offset += 2 + cipherSuitesLen

	// Compression Methods
	if offset >= len(msg) {
		return "", errNoSNI
	}
	compressionMethodsLen := int(msg[offset])
	offset += 1 + compressionMethodsLen

	// Extensions Length
	if offset+2 > len(msg) {
		return "", errNoSNI
	}
	extensionsLen := int(msg[offset])<<8 | int(msg[offset+1])
	offset += 2

	if offset+extensionsLen > len(msg) {
		extensionsLen = len(msg) - offset
	}

	exts := msg[offset : offset+extensionsLen]
	extOffset := 0

	for extOffset+4 <= len(exts) {
		extType := int(exts[extOffset])<<8 | int(exts[extOffset+1])
		extLen := int(exts[extOffset+2])<<8 | int(exts[extOffset+3])
		extOffset += 4

		if extOffset+extLen > len(exts) {
			break
		}

		if extType == 0 { // server_name (SNI)
			sniData := exts[extOffset : extOffset+extLen]
			if len(sniData) < 2 {
				return "", errNoSNI
			}
			listLen := int(sniData[0])<<8 | int(sniData[1])
			if len(sniData) < 2+listLen {
				return "", errNoSNI
			}
			snData := sniData[2 : 2+listLen]
			snOffset := 0
			for snOffset+3 <= len(snData) {
				nameType := snData[snOffset]
				nameLen := int(snData[snOffset+1])<<8 | int(snData[snOffset+2])
				snOffset += 3
				if snOffset+nameLen > len(snData) {
					break
				}
				if nameType == 0 { // host_name
					return string(snData[snOffset : snOffset+nameLen]), nil
				}
				snOffset += nameLen
			}
		}

		extOffset += extLen
	}

	return "", errNoSNI
}
