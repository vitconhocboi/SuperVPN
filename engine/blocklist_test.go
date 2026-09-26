package engine

import (
	"os"
	"testing"
)

func TestBlocklistMatch(t *testing.T) {
	bl := NewBlocklist()
	bl.AddDomain("doubleclick.net")
	bl.AddDomain("ads.google.com")

	tests := []struct {
		domain   string
		expected bool
	}{
		{"doubleclick.net", true},
		{"sub.doubleclick.net", true},
		{"a.b.sub.doubleclick.net", true},
		{"ads.google.com", true},
		{"sub.ads.google.com", true},
		{"google.com", false},
		{"other.net", false},
		{"click.net", false},
	}

	for _, tt := range tests {
		if got := bl.Match(tt.domain); got != tt.expected {
			t.Errorf("Match(%q) = %v; want %v", tt.domain, got, tt.expected)
		}
	}
}

func TestBlocklistPathRulesAndBypass(t *testing.T) {
	bl := NewBlocklist()
	bl.AddPathRule("example.com", "/ads/*")
	bl.AddPathRule("example.com", "/banner.png")

	// Test MatchDomain for example.com -> NeedsMITM = true
	res := bl.MatchDomain("example.com")
	if !res.NeedsMITM {
		t.Errorf("expected NeedsMITM true for example.com")
	}
	if res.IsBlockedDomain {
		t.Errorf("expected IsBlockedDomain false for example.com")
	}

	// Test MatchPath
	if !bl.MatchPath("example.com", "/ads/header.png") {
		t.Errorf("expected /ads/header.png to match path rule")
	}
	if !bl.MatchPath("example.com", "/banner.png") {
		t.Errorf("expected /banner.png to match path rule")
	}
	if bl.MatchPath("example.com", "/index.html") {
		t.Errorf("expected /index.html not to match path rule")
	}

	// Subdomains inherit the domain's path rules.
	if !bl.MatchDomain("cdn.example.com").NeedsMITM || !bl.MatchPath("cdn.example.com", "/ads/x") {
		t.Errorf("expected cdn.example.com to inherit example.com path rules")
	}
	// Prefix-anchored: a rule path in the middle of the request path does not match.
	if bl.MatchPath("example.com", "/x/banner.png") {
		t.Errorf("expected /x/banner.png not to match /banner.png")
	}
}

func TestMITMPassthroughOnlyGuardsMITM(t *testing.T) {
	bl := NewBlocklist() // facebook.com + banks are default passthrough
	bl.AddDomain("ads.facebook.com")
	bl.AddPathRule("facebook.com", "/tr")
	bl.AddPathRule("chase.com", "/promo")

	if !bl.Match("ads.facebook.com") {
		t.Errorf("passthrough must not disable domain blocking")
	}
	if bl.Match("www.facebook.com") {
		t.Errorf("www.facebook.com must stay reachable")
	}
	if bl.MatchDomain("www.facebook.com").NeedsMITM || bl.MatchDomain("chase.com").NeedsMITM {
		t.Errorf("passthrough domains must never need MITM")
	}
	if bl.MatchPath("chase.com", "/promo") {
		t.Errorf("passthrough domains must never be path-blocked")
	}
}

func TestAllowRuleOverridesBlock(t *testing.T) {
	bl := NewBlocklist()
	bl.AddRule("tracker.net")
	bl.AddRule("@@||good.tracker.net^")
	bl.AddRule("@@allowed.org")
	bl.AddRule("ads.allowed.org")

	if !bl.Match("x.tracker.net") {
		t.Errorf("expected x.tracker.net blocked")
	}
	if bl.Match("good.tracker.net") || bl.Match("a.good.tracker.net") {
		t.Errorf("@@ exception must unblock good.tracker.net and below")
	}
	if bl.Match("ads.allowed.org") {
		t.Errorf("@@ on a parent must win over a child block")
	}
}

func TestPathRuleEdgeForms(t *testing.T) {
	bl := NewBlocklist()
	bl.AddRule("whole.com/")     // bare slash degrades to domain block
	bl.AddRule("star.com/*")     // so does a match-all path
	bl.AddRule("two words/path") // malformed: ignored
	if !bl.Match("whole.com") || !bl.Match("star.com") {
		t.Errorf("match-all path rules must block the domain")
	}
	if bl.MatchDomain("two").NeedsMITM || bl.Match("two") {
		t.Errorf("malformed line must be ignored")
	}
}

func TestLoadBlocklistFromFile(t *testing.T) {
	content := `
# Comment line
0.0.0.0 adserver.com
127.0.0.1 tracker.analytics.org
badsite.cn
example.com/ads/*
@@chase.com
`
	tmp, err := os.CreateTemp("", "blocklist_test_*.txt")
	if err != nil {
		t.Fatal(err)
	}
	defer os.Remove(tmp.Name())

	if _, err := tmp.WriteString(content); err != nil {
		t.Fatal(err)
	}
	tmp.Close()

	bl, err := LoadBlocklistFromFile(tmp.Name())
	if err != nil {
		t.Fatalf("LoadBlocklistFromFile failed: %v", err)
	}

	if !bl.Match("adserver.com") {
		t.Errorf("expected adserver.com to match")
	}
	if !bl.Match("sub.tracker.analytics.org") {
		t.Errorf("expected sub.tracker.analytics.org to match")
	}
	if !bl.Match("badsite.cn") {
		t.Errorf("expected badsite.cn to match")
	}
	if bl.Match("goodsite.com") {
		t.Errorf("expected goodsite.com not to match")
	}
	if !bl.MatchPath("example.com", "/ads/banner") {
		t.Errorf("expected path rule to match")
	}
}
