# Ad-Block v2 System Architecture

## Data Path Overview

**Flow**: App TUN fd → Go netstack (gVisor) → layer-specific blocking decisions → forward or intercept

```
┌─────────────────────────────────────────────────────────────────┐
│ Apps                                                             │
└────────────────────┬────────────────────────────────────────────┘
                     │ TCP/UDP (all traffic)
                     ↓
┌─────────────────────────────────────────────────────────────────┐
│ Go Engine (in-process netstack via gomobile)                    │
├─────────────────────────────────────────────────────────────────┤
│ ▶ UDP:53 (DNS)           → ParseDNSQuery → MatchDomain          │
│   Match? NXDOMAIN(3) : forward to real DNS (protect()ed socket) │
│                                                                  │
│ ▶ TCP:443 (HTTPS)        → peek ClientHello → ExtractSNI        │
│   Match & !pathRules? → RST close                               │
│   Match & pathRules? → TerminateTLS (MITM) → relayHTTP/403      │
│   Pinned app? → record & passthrough                            │
│   Allowed? → forward with peeked bytes                          │
│                                                                  │
│ ▶ UDP:443 (QUIC)         → is Initial? ExtractQUICSNI           │
│   Match & !pathRules? → drop                                    │
│   Match & pathRules? → TCP passthrough or drop (tradeoff)       │
│   Allowed? → forward UDP direct                                 │
│                                                                  │
│ ▶ Other TCP/UDP          → forward direct (no inspection)       │
└─────────────────────────────────────────────────────────────────┘
```

## Blocklist Trie: Suffix Matching

**Structure**: `trieNode` with children by domain label (TLD-first). Every path from root to a node represents domain suffixes in reverse (so `ads.example.com` is walked: `com` → `example` → `ads`).

**Node state** (flags can combine):
- `blocked`: domain + all subdomains blocked at DNS/SNI layer.
- `allowed`: `@@domain` exception; overrides blocked on node + below.
- `noMITM`: domain never entered MITM (banking/payment apps, Facebook), but DNS/SNI blocking still applies.
- `pathRules`: list of path prefixes for MITM path filtering (e.g., `/ads/*`).

**Walk semantics** (line-by-line in `blocklist.go:walk()`):
1. Normalize domain → split by `.` → walk TLD-first.
2. Aggregate flags from **every node matching any suffix** (e.g., `ads.example.com` aggregates from `com`, `example`, `ads`).
3. Return `MatchResult`:
   - `IsBlockedDomain=true` if `blocked && !allowed`.
   - `NeedsMITM=true` if path rules exist and not `noMITM`.

**Inheritance**: Subdomains inherit parent's block/path rules unless an exception overrides (e.g., `ads.example.com` blocked, but `safe.ads.example.com` can be whitelisted with `@@safe.ads.example.com`).

## Rule Syntax & Parsing

| Rule Form | Meaning | Example |
|-----------|---------|---------|
| `domain` | Block domain + subdomains (DNS/SNI) | `doubleclick.net` |
| `0.0.0.0 domain` | Hosts-file format (same as above) | `0.0.0.0 doubleclick.net` |
| `@@domain` | Exception (allow if parent blocked) | `@@safe.ads.example.com` |
| `@@\|\|domain^` | AdBlock syntax (parsed to `@@domain`) | `@@\|\|analytics.com^` |
| `domain/path` | Path rule (MITM-only, prefix match) | `ads.example.com/banner` |
| `domain/path*` | Wildcard suffix (same as bare path) | `ads.example.com/ads/*` → domain block |
| `#` or `!` | Comment | `# Ad domain` |

**Validation** (DomainValidator.kt): ASCII/punycode, lowercase, no wildcards, no IPs, path chars only (RFC 3986 pchar). Max 253 chars domain, 512 chars path. Dropped at render time (BlocklistGenerator.render()).

## MITM Flow & Limitations

**When triggered**: TCP:443 with SNI matching a domain that has path rules and is not in `noMITM` passthrough list (default: Facebook, Instagram, WhatsApp, banking apps).

**Process** (handleSelectiveMITM in mitm.go):
1. Generate leaf cert signed by CA for SNI.
2. Advertise `http/1.1` only (no h2 negotiation).
3. Client TLS handshake (3s timeout).
   - If cert rejected → mark host as pinned (mitm_pinning.go) → passthrough forever.
   - If 3+ silent closes → passthrough (apps pinning app-internal CAs).
4. Relay HTTP/1.1 requests 1-by-1 (relayHTTP in mitm.go):
   - Per request: match path against rules → 403 Forbidden if blocked, else forward.
   - Keep-alive sessions supported; upgrades (WebSocket, etc.) fail cleanly.
5. Close on error or client EOF.

**Limitations** (by design):
- **HTTP/1.1 only**: h2 not advertised; HTTP/3 blocked by not intercepting QUIC. P2 task 6 documents tradeoff: domain with path rules can optionally pin to TCP (drop UDP:443) so all traffic uses MITM-able HTTP/1.1.
- **User CA trust required**: Certificate chain must end at a CA in the system/user trust store. Android 11+ requires manual install (Settings → Encryption & credentials). Apps pinning own CAs (or Google's Play Services cert) auto-bypass.
- **ECH (Encrypted Client Hello)**: SNI hidden; no fallback in practice. Rare for ad domains today.
- **QUIC path filtering**: Path rules on QUIC hosts not possible (keys unknown). Mitigation: pinning to TCP.

## CA Persistence & Security

**Lifecycle** (MitmCaStore.kt):
1. **First use**: Call `engine.Engine.generateCA()` → RSA-2048 self-signed CA + key (engine/ca.go).
2. **Encryption**: Private key encrypted with Android Keystore AES-256-GCM (non-exportable key).
3. **Storage**: `noBackupFilesDir/mitm_ca.crt` (public cert), `mitm_ca.key.enc` (encrypted key).
4. **Reload on error**: If decrypt fails (e.g., device restored) → generate fresh CA (user must reinstall cert).

**Security notes**:
- Key material **never** leaves Keystore; Go side receives PEM strings at startup.
- Encrypted key format: `[1-byte IV len][IV][ciphertext+tag]`.
- Cert public and safe to share (included in P3 debug log download).

## Rule Compilation Pipeline

```
Room AdRuleDB (user rules + defaults)
    ↓ [AdsBlockRepository.renderBlocklist()]
Enabled domains list (per crashGuard level)
    ↓ [BlocklistGenerator.render()]
Re-validate each rule (DomainValidator.validate)
    ↓ [EngineBlocklistFile.write() — atomic rename]
blocklist.txt (engine/blocklist.go line format)
    ↓ [LocalVpnService.Start() — Load or hot reload]
Go Blocklist Trie (populated, atomic swap on reload)
    ↓ [TCP/UDP/DNS decision handlers]
Blocking decisions
```

**Hot reload**: `AdsRuleReapplier.reapply()` → write file → call `engine.Engine.reloadBlocklist()`.

## Testing & Verification

**Go unit tests** (run from engine/):
```bash
cd engine
go test ./...  # blocklist_test.go: trie walk, path rules, exceptions, pinning list
```

**Kotlin tests** (app/src/test):
```bash
./gradlew :app:testDevDebugUnitTest --offline --tests "com.tici.vpn.proxy.master.settings.adsblock.*"
```

**Device (acceptance)**:
- Facebook app/web with VPN on (no UDP drop).
- Blocked domain fails on TCP & QUIC paths.
- QUIC-pinned ad domain blocked (drop or passthrough test).
- Banking app + MITM CA installed (path rule not invoked, pinning list guards).
- WiFi ↔ data switch (no crash).

## Known Dead Code & Future Cleanup

Pending removal (not yet deleted; breaks builds if removed early):
- `app/libs/tun2socks.aar`, `tun2socks.jar` (replaced by netstack.aar).
- `VpnManager.kt` (legacy manager, unused after P1).
- `PrivoxyActionFile`, native Privoxy C build in `cpp/privoxy/` (config file generator, entire proxy removed).

`DefaultAdRulesParser.kt` kept for domain canonicalization (`Domain.canonicalize()` pattern); its old action-file parsing dead.
