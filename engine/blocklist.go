package engine

import (
	"bufio"
	"os"
	"strings"
	"sync/atomic"
)

// defaultMITMPassthrough lists domains (and their subdomains) that are never
// MITM'd even when a path rule targets them: banking/payment apps pin
// certificates and break under interception. Domain-level blocking still
// applies to them — this list only guards the MITM layer.
var defaultMITMPassthrough = []string{
	"chase.com", "bankofamerica.com", "wellsfargo.com", "citibank.com",
	"paypal.com", "stripe.com", "capitalone.com", "fidelity.com",
	"facebook.com", "instagram.com", "whatsapp.com",
}

type trieNode struct {
	children map[string]*trieNode
	// blocked: domain rule — this node and all subdomains are blocked.
	blocked bool
	// allowed: `@@` exception — overrides any block on this node and below.
	allowed bool
	// noMITM: never intercept this node and below (passthrough whitelist).
	noMITM    bool
	pathRules []string
}

// Blocklist is an immutable-after-load suffix Trie keyed by domain labels
// (TLD first). Reloads build a fresh instance and swap it atomically.
type Blocklist struct {
	root *trieNode
}

// MatchResult is the domain-level verdict for one hostname.
type MatchResult struct {
	IsBlockedDomain bool
	// NeedsMITM: path rules exist for this host and it is not whitelisted.
	NeedsMITM bool
}

func newTrieNode() *trieNode {
	return &trieNode{children: make(map[string]*trieNode)}
}

func NewBlocklist() *Blocklist {
	bl := &Blocklist{root: newTrieNode()}
	for _, d := range defaultMITMPassthrough {
		bl.AddMITMPassthrough(d)
	}
	return bl
}

func normalizeDomain(domain string) string {
	return strings.TrimSuffix(strings.ToLower(strings.TrimSpace(domain)), ".")
}

// node walks (creating as needed) to the node for domain; nil if empty.
func (b *Blocklist) node(domain string) *trieNode {
	domain = normalizeDomain(domain)
	if domain == "" {
		return nil
	}
	parts := strings.Split(domain, ".")
	curr := b.root
	for i := len(parts) - 1; i >= 0; i-- {
		if parts[i] == "" {
			continue
		}
		child, ok := curr.children[parts[i]]
		if !ok {
			child = newTrieNode()
			curr.children[parts[i]] = child
		}
		curr = child
	}
	return curr
}

// AddRule parses one line: `domain`, hosts-file `0.0.0.0 domain`,
// `domain/path` (path rule), or `@@domain` / `@@||domain^` (exception).
func (b *Blocklist) AddRule(raw string) {
	raw = strings.TrimSpace(raw)
	if raw == "" || strings.HasPrefix(raw, "#") || strings.HasPrefix(raw, "!") {
		return
	}

	if strings.HasPrefix(raw, "@@") {
		d := strings.TrimPrefix(raw, "@@")
		d = strings.TrimPrefix(d, "||")
		d = strings.TrimSuffix(d, "^")
		b.AddAllowDomain(d)
		return
	}

	fields := strings.Fields(raw)
	if len(fields) >= 2 && (fields[0] == "0.0.0.0" || fields[0] == "127.0.0.1") {
		b.AddDomain(fields[1])
		return
	}
	if len(fields) != 1 {
		return
	}

	if idx := strings.Index(raw, "/"); idx != -1 {
		b.AddPathRule(raw[:idx], raw[idx:])
		return
	}
	b.AddDomain(raw)
}

func (b *Blocklist) AddDomain(domain string) {
	if n := b.node(domain); n != nil {
		n.blocked = true
	}
}

func (b *Blocklist) AddAllowDomain(domain string) {
	if n := b.node(domain); n != nil {
		n.allowed = true
	}
}

func (b *Blocklist) AddMITMPassthrough(domain string) {
	if n := b.node(domain); n != nil {
		n.noMITM = true
	}
}

// AddPathRule registers a path prefix (optionally ending in `*`) for domain
// and its subdomains. A bare `/` or empty path degrades to a domain block.
func (b *Blocklist) AddPathRule(domain, pathRule string) {
	pathRule = strings.TrimSpace(pathRule)
	if pathRule == "" || pathRule == "/" || pathRule == "/*" {
		b.AddDomain(domain)
		return
	}
	if n := b.node(domain); n != nil {
		n.pathRules = append(n.pathRules, pathRule)
	}
}

// walkResult aggregates flags along every Trie node matching domain's suffixes.
type walkResult struct {
	blocked, allowed, noMITM bool
	pathRules                []string
}

func (b *Blocklist) walk(domain string) walkResult {
	var w walkResult
	if b == nil || b.root == nil {
		return w
	}
	domain = normalizeDomain(domain)
	if domain == "" {
		return w
	}
	parts := strings.Split(domain, ".")
	curr := b.root
	for i := len(parts) - 1; i >= 0; i-- {
		child, ok := curr.children[parts[i]]
		if !ok {
			break
		}
		curr = child
		w.blocked = w.blocked || curr.blocked
		w.allowed = w.allowed || curr.allowed
		w.noMITM = w.noMITM || curr.noMITM
		w.pathRules = append(w.pathRules, curr.pathRules...)
	}
	return w
}

func (b *Blocklist) MatchDomain(domain string) MatchResult {
	w := b.walk(domain)
	if w.allowed {
		return MatchResult{}
	}
	if w.blocked {
		return MatchResult{IsBlockedDomain: true}
	}
	return MatchResult{NeedsMITM: len(w.pathRules) > 0 && !w.noMITM}
}

// MatchPath reports whether an intercepted request path on domain is blocked.
func (b *Blocklist) MatchPath(domain, path string) bool {
	w := b.walk(domain)
	if w.allowed || w.noMITM {
		return false
	}
	for _, rule := range w.pathRules {
		if matchPathRule(rule, path) {
			return true
		}
	}
	return false
}

// matchPathRule is a prefix match, Privoxy-style (anchored at path start);
// a trailing `*` is accepted and means the same thing.
func matchPathRule(rule, path string) bool {
	return strings.HasPrefix(path, strings.TrimSuffix(rule, "*"))
}

func (b *Blocklist) Match(domain string) bool {
	return b.MatchDomain(domain).IsBlockedDomain
}

func LoadBlocklistFromFile(filePath string) (*Blocklist, error) {
	f, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	bl := NewBlocklist()
	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		bl.AddRule(scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		return nil, err
	}
	return bl, nil
}

var globalBlocklist atomic.Pointer[Blocklist]

func getBlocklist() *Blocklist {
	return globalBlocklist.Load()
}

func setBlocklist(bl *Blocklist) {
	globalBlocklist.Store(bl)
}
