package engine

import (
	"crypto"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/tls"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/pem"
	"errors"
	"fmt"
	"math/big"
	"net"
	"sync"
	"sync/atomic"
	"time"
)

const (
	caValidity   = 10 * 365 * 24 * time.Hour
	leafValidity = 365 * 24 * time.Hour
)

// CAPEM is the gomobile-friendly serialized form of the MITM root CA. The
// Kotlin side persists it (key encrypted with an Android Keystore key) so the
// certificate the user installed stays valid across restarts.
type CAPEM struct {
	CertPEM string
	KeyPEM  string // PKCS#8 private key
}

// CA signs per-host leaf certificates for selective MITM.
type CA struct {
	cert      *x509.Certificate
	key       crypto.Signer
	certCache sync.Map // host -> *tls.Certificate
}

// GenerateCA creates a fresh self-signed root CA (P-256) for persistence.
func GenerateCA() (*CAPEM, error) {
	ca, err := newCA()
	if err != nil {
		return nil, err
	}
	return ca.PEM()
}

func newSerial() (*big.Int, error) {
	return rand.Int(rand.Reader, new(big.Int).Lsh(big.NewInt(1), 128))
}

func newCA() (*CA, error) {
	priv, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return nil, err
	}
	serial, err := newSerial()
	if err != nil {
		return nil, err
	}
	template := x509.Certificate{
		SerialNumber: serial,
		Subject: pkix.Name{
			Organization: []string{"SuperVPN AdBlock CA"},
			CommonName:   "SuperVPN Root CA",
		},
		NotBefore:             time.Now().Add(-24 * time.Hour),
		NotAfter:              time.Now().Add(caValidity),
		KeyUsage:              x509.KeyUsageCertSign | x509.KeyUsageCRLSign | x509.KeyUsageDigitalSignature,
		BasicConstraintsValid: true,
		IsCA:                  true,
		MaxPathLenZero:        true,
	}
	der, err := x509.CreateCertificate(rand.Reader, &template, &template, &priv.PublicKey, priv)
	if err != nil {
		return nil, err
	}
	cert, err := x509.ParseCertificate(der)
	if err != nil {
		return nil, err
	}
	return &CA{cert: cert, key: priv}, nil
}

// PEM serializes the CA certificate and private key.
func (ca *CA) PEM() (*CAPEM, error) {
	keyDER, err := x509.MarshalPKCS8PrivateKey(ca.key)
	if err != nil {
		return nil, err
	}
	return &CAPEM{
		CertPEM: string(pem.EncodeToMemory(&pem.Block{Type: "CERTIFICATE", Bytes: ca.cert.Raw})),
		KeyPEM:  string(pem.EncodeToMemory(&pem.Block{Type: "PRIVATE KEY", Bytes: keyDER})),
	}, nil
}

// parseCA restores a CA from PEM and checks the key matches the certificate.
func parseCA(certPEM, keyPEM string) (*CA, error) {
	certBlock, _ := pem.Decode([]byte(certPEM))
	if certBlock == nil || certBlock.Type != "CERTIFICATE" {
		return nil, errors.New("ca: no CERTIFICATE block")
	}
	cert, err := x509.ParseCertificate(certBlock.Bytes)
	if err != nil {
		return nil, fmt.Errorf("ca: parse cert: %w", err)
	}
	if !cert.IsCA {
		return nil, errors.New("ca: certificate is not a CA")
	}
	keyBlock, _ := pem.Decode([]byte(keyPEM))
	if keyBlock == nil {
		return nil, errors.New("ca: no private key block")
	}
	parsed, err := x509.ParsePKCS8PrivateKey(keyBlock.Bytes)
	if err != nil {
		return nil, fmt.Errorf("ca: parse key: %w", err)
	}
	signer, ok := parsed.(crypto.Signer)
	if !ok {
		return nil, errors.New("ca: key is not a signer")
	}
	pub, ok := signer.Public().(interface{ Equal(crypto.PublicKey) bool })
	if !ok || !pub.Equal(cert.PublicKey) {
		return nil, errors.New("ca: key does not match certificate")
	}
	return &CA{cert: cert, key: signer}, nil
}

// GetServerCertificate returns a cached or freshly signed leaf for host.
func (ca *CA) GetServerCertificate(host string) (*tls.Certificate, error) {
	if ca == nil {
		return nil, errors.New("CA not initialized")
	}
	if val, ok := ca.certCache.Load(host); ok {
		return val.(*tls.Certificate), nil
	}

	priv, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return nil, err
	}
	serial, err := newSerial()
	if err != nil {
		return nil, err
	}
	notAfter := time.Now().Add(leafValidity)
	if notAfter.After(ca.cert.NotAfter) {
		notAfter = ca.cert.NotAfter
	}
	template := x509.Certificate{
		SerialNumber: serial,
		Subject: pkix.Name{
			Organization: []string{"SuperVPN MITM"},
			CommonName:   host,
		},
		NotBefore:   time.Now().Add(-24 * time.Hour),
		NotAfter:    notAfter,
		KeyUsage:    x509.KeyUsageDigitalSignature,
		ExtKeyUsage: []x509.ExtKeyUsage{x509.ExtKeyUsageServerAuth},
	}
	if ip := net.ParseIP(host); ip != nil {
		template.IPAddresses = []net.IP{ip}
	} else {
		template.DNSNames = []string{host}
	}

	der, err := x509.CreateCertificate(rand.Reader, &template, ca.cert, &priv.PublicKey, ca.key)
	if err != nil {
		return nil, err
	}
	leaf := &tls.Certificate{
		Certificate: [][]byte{der, ca.cert.Raw},
		PrivateKey:  priv,
	}
	actual, _ := ca.certCache.LoadOrStore(host, leaf)
	return actual.(*tls.Certificate), nil
}

// activeCA is the CA loaded from Key at Start; nil disables MITM entirely
// (path-rule domains are then forwarded untouched).
var activeCA atomic.Pointer[CA]

func getCA() *CA { return activeCA.Load() }

// loadCA installs the CA from key material; empty input disables MITM.
func loadCA(certPEM, keyPEM string) error {
	if certPEM == "" || keyPEM == "" {
		activeCA.Store(nil)
		return nil
	}
	ca, err := parseCA(certPEM, keyPEM)
	if err != nil {
		activeCA.Store(nil)
		return err
	}
	activeCA.Store(ca)
	return nil
}
