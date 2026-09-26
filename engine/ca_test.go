package engine

import (
	"crypto/x509"
	"testing"
	"time"
)

func TestCAPEMRoundTripAndSigning(t *testing.T) {
	p, err := GenerateCA()
	if err != nil {
		t.Fatalf("GenerateCA: %v", err)
	}
	ca, err := parseCA(p.CertPEM, p.KeyPEM)
	if err != nil {
		t.Fatalf("parseCA: %v", err)
	}

	leaf, err := ca.GetServerCertificate("ads.example.com")
	if err != nil {
		t.Fatalf("GetServerCertificate: %v", err)
	}
	cert, err := x509.ParseCertificate(leaf.Certificate[0])
	if err != nil {
		t.Fatal(err)
	}
	roots := x509.NewCertPool()
	roots.AddCert(ca.cert)
	if _, err := cert.Verify(x509.VerifyOptions{DNSName: "ads.example.com", Roots: roots, CurrentTime: time.Now()}); err != nil {
		t.Errorf("leaf must verify against the restored CA: %v", err)
	}

	again, _ := ca.GetServerCertificate("ads.example.com")
	if again != leaf {
		t.Errorf("expected cached leaf on second call")
	}
}

func TestParseCARejectsMismatchedKey(t *testing.T) {
	a, _ := GenerateCA()
	b, _ := GenerateCA()
	if _, err := parseCA(a.CertPEM, b.KeyPEM); err == nil {
		t.Errorf("expected error for key not matching cert")
	}
	if _, err := parseCA("garbage", a.KeyPEM); err == nil {
		t.Errorf("expected error for invalid cert PEM")
	}
}

func TestLoadCAEmptyDisablesMITM(t *testing.T) {
	p, _ := GenerateCA()
	if err := loadCA(p.CertPEM, p.KeyPEM); err != nil || getCA() == nil {
		t.Fatalf("expected CA loaded, err=%v", err)
	}
	if err := loadCA("", ""); err != nil || getCA() != nil {
		t.Errorf("empty PEM must disable MITM")
	}
	if err := loadCA("bad", "bad"); err == nil || getCA() != nil {
		t.Errorf("invalid PEM must error and disable MITM")
	}
}
