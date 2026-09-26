package engine

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/sha256"
	"encoding/binary"
	"encoding/hex"
	"errors"
	"io"

	"golang.org/x/crypto/hkdf"
)

var (
	quicV1InitialSalt, _ = hex.DecodeString("38762cf7f55934b34d179ae6a4c80cadccbb7f0a")
	quicV2InitialSalt, _ = hex.DecodeString("0dede3def700a6db819381be6e269dcbf9bd2ed16456be1a6c2965114a09e0a8")
)

var errNotQUICInitial = errors.New("not a QUIC initial packet")

func hkdfExpandLabel(secret []byte, label string, context []byte, length int) []byte {
	hkdfLabel := make([]byte, 0, 2+1+len("tls13 ")+len(label)+1+len(context))
	hkdfLabel = binary.BigEndian.AppendUint16(hkdfLabel, uint16(length))
	fullLabel := "tls13 " + label
	hkdfLabel = append(hkdfLabel, byte(len(fullLabel)))
	hkdfLabel = append(hkdfLabel, fullLabel...)
	hkdfLabel = append(hkdfLabel, byte(len(context)))
	hkdfLabel = append(hkdfLabel, context...)

	out := make([]byte, length)
	r := hkdf.Expand(sha256.New, secret, hkdfLabel)
	_, _ = io.ReadFull(r, out)
	return out
}

func readVarint(data []byte, offset int) (uint64, int, error) {
	if offset >= len(data) {
		return 0, 0, errNotQUICInitial
	}
	first := data[offset]
	v := uint64(first & 0x3f)
	length := 1 << ((first & 0xc0) >> 6)
	if offset+length > len(data) {
		return 0, 0, errNotQUICInitial
	}
	for i := 1; i < length; i++ {
		v = (v << 8) | uint64(data[offset+i])
	}
	return v, length, nil
}

// ExtractQUICSNI attempts to extract SNI from a QUIC Initial datagram.
// Fail-open: returns errNoSNI or errNotQUICInitial if packet is not an Initial or cannot be parsed.
func ExtractQUICSNI(packet []byte) (string, error) {
	if len(packet) < 1200 {
		return "", errNotQUICInitial
	}

	firstByte := packet[0]
	if (firstByte&0x80) == 0 || (firstByte&0x40) == 0 {
		return "", errNotQUICInitial
	}

	longType := (firstByte & 0x30) >> 4
	if longType != 0x00 {
		return "", errNotQUICInitial
	}

	version := binary.BigEndian.Uint32(packet[1:5])
	var salt []byte
	if version == 0x00000001 {
		salt = quicV1InitialSalt
	} else if version == 0x6b333833 || version == 0x709a50c4 {
		salt = quicV2InitialSalt
	} else {
		return "", errNotQUICInitial
	}

	dcidLen := int(packet[5])
	if dcidLen <= 0 || 6+dcidLen > len(packet) {
		return "", errNotQUICInitial
	}
	offset := 6
	dcid := packet[offset : offset+dcidLen]
	offset += dcidLen

	if offset >= len(packet) {
		return "", errNotQUICInitial
	}
	scidLen := int(packet[offset])
	offset += 1 + scidLen

	if offset > len(packet) {
		return "", errNotQUICInitial
	}

	tokenLen, vlen, err := readVarint(packet, offset)
	if err != nil {
		return "", err
	}
	offset += vlen + int(tokenLen)

	payloadLen, vlen, err := readVarint(packet, offset)
	if err != nil {
		return "", err
	}
	offset += vlen

	if offset+int(payloadLen) > len(packet) {
		return "", errNotQUICInitial
	}

	secret := hkdf.Extract(sha256.New, dcid, salt)
	key := hkdfExpandLabel(secret, "client in", nil, 16)
	iv := hkdfExpandLabel(secret, "quic iv", nil, 12)
	hpKey := hkdfExpandLabel(secret, "quic hp", nil, 16)

	sampleOffset := offset + 4
	if sampleOffset+16 > len(packet) {
		return "", errNotQUICInitial
	}
	sample := packet[sampleOffset : sampleOffset+16]

	hpBlock, err := aes.NewCipher(hpKey)
	if err != nil {
		return "", err
	}
	mask := make([]byte, 16)
	hpBlock.Encrypt(mask, sample)

	unprotectedFirst := firstByte ^ (mask[0] & 0x0f)
	pnLen := int(unprotectedFirst&0x03) + 1

	pnBytes := make([]byte, pnLen)
	for i := 0; i < pnLen; i++ {
		pnBytes[i] = packet[offset+i] ^ mask[1+i]
	}

	var packetNum uint64
	for _, b := range pnBytes {
		packetNum = (packetNum << 8) | uint64(b)
	}

	payloadOffset := offset + pnLen
	cipherTextLen := int(payloadLen) - pnLen
	if payloadOffset+cipherTextLen > len(packet) {
		return "", errNotQUICInitial
	}
	cipherText := packet[payloadOffset : payloadOffset+cipherTextLen]

	nonce := make([]byte, 12)
	copy(nonce, iv)
	binary.BigEndian.PutUint64(nonce[4:], binary.BigEndian.Uint64(nonce[4:])^packetNum)

	aeadBlock, err := aes.NewCipher(key)
	if err != nil {
		return "", err
	}
	aead, err := cipher.NewGCM(aeadBlock)
	if err != nil {
		return "", err
	}

	hdrAD := make([]byte, payloadOffset)
	copy(hdrAD, packet[:payloadOffset])
	hdrAD[0] = unprotectedFirst
	copy(hdrAD[offset:payloadOffset], pnBytes)

	plaintext, err := aead.Open(nil, nonce, cipherText, hdrAD)
	if err != nil {
		return "", errNotQUICInitial
	}

	ptOffset := 0
	for ptOffset < len(plaintext) {
		frameType := plaintext[ptOffset]
		if frameType == 0x00 {
			ptOffset++
			continue
		}
		if frameType == 0x06 {
			ptOffset++
			_, vlen1, err1 := readVarint(plaintext, ptOffset)
			if err1 != nil {
				break
			}
			ptOffset += vlen1
			cryptoLen, vlen2, err2 := readVarint(plaintext, ptOffset)
			if err2 != nil {
				break
			}
			ptOffset += vlen2
			if ptOffset+int(cryptoLen) > len(plaintext) {
				break
			}
			cryptoData := plaintext[ptOffset : ptOffset+int(cryptoLen)]
			return ExtractSNI(cryptoData)
		}
		break
	}

	return "", errNoSNI
}
