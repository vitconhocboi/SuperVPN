# Ad-Block v2 Codebase Summary

## Go Engine Structure (gVisor Netstack)

**Package**: `engine/` (gomobile bound for Android JNI).

### Core Files & Responsibilities

**engine.go** — Singleton lifecycle:
- `Key`: one-shot config (MTU, TUN fd, DNSServers, BlocklistPath, CACertPEM, CAKeyPEM).
- `Insert(key)`: queue config; `Start()`: bring up netstack; `Stop()`: teardown.
- `EngineStats`: point-in-time snapshot (DNSBlocked, TCPBlocked, QUICBlocked, MITMBlocked, Forwarded, ActiveConns, Errored).
- Goroutine per flow; WaitGroup per Start..Stop cycle (restart-safe).

**blocklist.go** — Suffix Trie (O(log n) per lookup):
- `Blocklist`: root node, atomic swap on reload.
- `trieNode`: children map (by label), blocked/allowed/noMITM/pathRules flags.
- `AddRule(raw)`: parse line (`domain`, `0.0.0.0 domain`, `domain/path`, `@@domain`), insert into Trie.
- `MatchDomain(domain)`: walk Trie TLD-first, aggregate flags, return `MatchResult`.
- `MatchPath(domain, path)`: prefix-match path against path rules.
- `defaultMITMPassthrough[]`: Facebook, Instagram, WhatsApp, banks (never MITM'd even if path rules exist).

**tcp.go** — TCP flow handling:
- `handleTCPFlow(conn)`: read peeked bytes (3s deadline), extract SNI, check blocklist.
- Decision tree: `IsBlockedDomain` → close; `NeedsMITM` → intercept; else → forward.
- Forward: `net.Dial` to target, bidirectional pipe with peeked bytes prepended.
- Stats: `TCPBlocked`, `Forwarded`, `ForwardedTCP`, `ActiveConns`.

**quic.go** — QUIC Initial SNI extraction:
- `ExtractQUICSNI(packet)`: RFC 9001 Initial packet detection + DCID decryption.
- Supports QUIC v1 (salt `38762cf7f5...`) and v2 (salt `0dede3de...`).
- Fail-open: non-Initial packets or parse errors → forwarded as UDP.
- Returns SNI string or error; UDP handler drops if blocked, forwards if allowed.

**dns.go** — DNS interception (UDP:53):
- `ParseDNSQuery(data)`: extract transaction ID, domain, query type (A/AAAA/etc).
- `BuildNXDomainResponse(query)`: synthesize NXDOMAIN (flags=0x8183, RCODE=3).
- Logic: match domain → NXDOMAIN response; miss → forward to configured DNS (protect()ed socket).
- Transaction ID preserved in response.

**tls.go** — TLS ClientHello parser:
- `ExtractSNI(data)`: parse TLS record (type 0x16), ClientHello (0x01), extract SNI extension.
- Fail-open: not TLS or missing SNI → error (upstream handler then forwards).

**ca.go** — MITM CA generation & signing:
- `GenerateCA()`: RSA-2048 self-signed root cert + key, valid 10 years.
- `GetServerCertificate(sni)`: generate leaf cert (RSA-2048, 1 year), signed by CA, CN=SNI.
- Caching: avoid re-signing same SNI (memory cache with expiry).

**mitm.go, mitm_pinning.go** — Selective MITM:
- `handleSelectiveMITM(clientConn, sni, targetAddr, peeked, bl, ca)`: terminate client TLS (leaf cert), relay HTTP/1.1 requests, answer 403 for matched paths.
- `relayHTTP(client, dial, host, bl)`: read HTTP request per keep-alive, check path rule, forward or block.
- `mitm_pinning.go`: track hosts that rejected our cert (3-strike silent-close rule) → auto-passthrough; clear after 1h idle.

**peeked_conn.go** — Streaming with peeked bytes:
- `peekedConn`: wraps a Conn, prepends peeked bytes before first Read (byte-safe forwarding after SNI peek).

**idle_conn.go** — Keep-alive timeout wrapper:
- `withIdleTimeout(conn, dur)`: wrap connection, reset deadline on every Read/Write, close on idle.

**stats.go** — Thread-safe stats (atomic counters):
- `globalStats`: DNSBlocked, TCPBlocked, QUICBlocked, MITMBlocked, Forwarded, ForwardedTCP, ForwardedUDP, ActiveConns, Errored.

**udp.go** — UDP relay (non-DNS, non-QUIC):
- `handleUDPFlow(addr, data)`: forward direct via protect()ed socket (games, VoIP, WebRTC).
- Stats: `Forwarded`, `ForwardedUDP`.

### Build & Deployment

**build-android.bat**: gomobile bind command
```cmd
gomobile bind -androidapi 27 -target android/arm,android/arm64,android/amd64 -o ../app/libs/netstack.aar .
```
Output: `netstack.aar` (JNI bindings + Go runtime; size ~8-12 MB).

**Note**: NDK path, Go version, gomobile setup are env-dependent. Windows command execution may hang on AutoRun registry entries; workaround: run from cmd.exe directly, not PowerShell.

## Android App Side (Kotlin)

### Key Kotlin Files

**network/LocalVpnService.kt** — VPN lifecycle:
- `onStartCommand(intent)`: check intent action (START/STOP), spin VPN thread, setup TUN fd.
- `writeBlocklist()`: call `adsBlockRepository.renderBlocklist()` → `EngineBlocklistFile.write()`, pass path to engine.
- `run()`: build VPN interface (10.0.0.2/32, full route, split tunnel logic), fd handover to engine, Start(key).
- `stopVPN()`: engine.Stop(), close fd, notify listeners.

**network/EngineBlocklistFile.kt** — Atomic blocklist writes:
- `write(context, text)`: temp file + atomic rename (so engine never loads half-written file).
- `file(context)`: `context.filesDir/blocklist.txt`.

**network/MitmCaStore.kt** — CA persistence:
- `loadOrCreate(context)`: check `noBackupFilesDir` for cert/key; if missing, call `engine.Engine.generateCA()`, encrypt key, persist.
- `encrypt/decrypt`: AES-256-GCM with Android Keystore (non-exportable wrap key).

**settings/adsblock/AdsBlockRepository.kt** — Room data layer:
- `getEnabledDomains()`: fetch enabled rules from `AdRuleDB` DAO.
- `renderBlocklist()`: check crashGuard level, call `BlocklistGenerator.render()`, return text.
- `addRule(domain)`: insert to Room, clear quarantine.
- All I/O on `Dispatchers.IO`.

**settings/adsblock/BlocklistGenerator.kt** — Compilation:
- `render(rules)`: validate each rule via `DomainValidator.validate()`, build `Set<String>` (dedup), format as `# Header\nrule1\nrule2\n...`.
- Drops invalid rules, returns `RenderResult(text, droppedCount)`.

**settings/adsblock/DomainValidator.kt** — Rule validation:
- `validate(raw)`: normalize (trim quotes, remove scheme, lowercase), parse host + path, IDN.toASCII(), regex match (domain labels, TLD, path chars).
- Returns `Valid(canonical)` or `Invalid(reason)`.

**settings/adsblock/AdsBlockFragment.kt** — UI:
- Toggle ad-block, list rules, add/remove/restore, show stats.
- Triggers `AdsRuleReapplier.reapply()` on changes.

**settings/adsblock/AdsRuleReapplier.kt** — Live reload:
- `reapply()`: render blocklist → write file → call `engine.Engine.reloadBlocklist()` (atomically swap Trie in Go).

**settings/adsblock/AdRulesCrashGuard.kt** — Quarantine on crash:
- Track crash count per rule (if crash count > threshold → disable rules). Levels: ALL_RULES (full set), DEFAULTS_ONLY (only default ad domains), DISABLED (empty blocklist).

**AdsBlockInterface.kt** — Abstraction:
- `renderBlocklist()`, `getEnabledDomains()`, `addRule()`, `delete()`, etc.

### Data Flow: App → Engine

```
1. User clicks "Ad-block" toggle → AdsBlockFragment.onToggle()
   ↓
2. AdsBlockRepository.renderBlocklist() [Dispatchers.IO]
   - getEnabledDomains() from Room AdRuleDB
   ↓
3. BlocklistGenerator.render(rules)
   - validate each rule (DomainValidator.validate)
   - dedup, format text
   ↓
4. EngineBlocklistFile.write(context, text)
   - atomic rename: tmp → blocklist.txt
   ↓
5. AdsRuleReapplier.reapply() → engine.Engine.reloadBlocklist()
   - Go side: atomic.SwapPointer(&blocklist, newBlocklist)
   ↓
6. Next TCP/UDP/DNS flow uses new Trie
```

**At VPN Start**:
```
LocalVpnService.onStartCommand()
  ↓ [VPN thread]
writeBlocklist() → adsBlockRepository.renderBlocklist()
  ↓
EngineBlocklistFile.write()
  ↓
engine.Start(key) where key.BlocklistPath = "...blocklist.txt"
```

## Test Coverage & Key Tests

**Go tests** (engine/):
- `blocklist_test.go`: Trie walk (domain inheritance, exception precedence), path rule matching, MITM passthrough semantics, file loading.
- `tls_test.go`, `dns_test.go`, `quic_test.go`: parsers.
- `ca_test.go`: cert generation, signing.
- `mitm_test.go`: request relay, path blocking, pinning.

**Kotlin tests** (app/src/test/java/com/tici/vpn/proxy/master/):
- `DomainValidatorTest.kt`: valid/invalid rules, edge cases (IDN, ports, schemes, paths).
- `BlocklistGeneratorTest.kt`: render, dedup, invalid dropping.
- `AdsBlockRepositoryTest.kt`: Room insertion, deletion, render.

**Device acceptance** (P1 P2):
- Facebook app/web reachable.
- Blocked domain fails TCP + QUIC.
- Banking app + MITM CA installed (path rules not invoked).
- WiFi ↔ data no crash.

## Dead Code Pending Removal

| File | Role | Status |
|------|------|--------|
| `app/libs/tun2socks.aar`, `.jar` | Old UDP-drop proxy | Replaced by netstack.aar (P1) |
| `VpnManager.kt` | Legacy manager | Unused (merged into LocalVpnService) |
| `PrivoxyActionFile` (removed in P2 rename to BlocklistGenerator) | Config generator for vendored Privoxy | Entire Privoxy C stack removed (P1) |
| `app/src/main/cpp/privoxy/` | Vendored Privoxy source | Removed in P1 build cleanup |
| `DefaultAdRulesParser.kt` (old methods) | Privoxy action-file parser | `canonicalize()` kept; old parsing dead |

**Cleanup timing**: After device testing validates P2 acceptance, these are safe to delete in a commit (no functional impact).

## Build & Dependencies

**Go**:
- `golang.org/x/net` (http2 for relay).
- `golang.org/x/crypto` (HKDF for QUIC SNI).
- `gvisor.dev/gvisor` (netstack, tun2socks adapter).
- `github.com/xjasonlyu/tun2socks/v2` (device/fd glue).
- **P2**: `github.com/lqqyt2423/go-mitmproxy` (MITM spike, optional).

**Kotlin** (Android):
- Room (AdRuleDB), Hilt (DI), Timber (logging).
- AndroidKeystore (AES-GCM for CA key wrap).

**Android NDK**: r21+ (CMakeLists.txt Go toolchain integration).

**Gradle**: `app/build.gradle.kts` excludes old proxy, adds netstack.aar.

## Key Design Decisions & Tradeoffs

| Decision | Rationale |
|----------|-----------|
| In-process netstack (Go) | No subprocess overhead; direct tun fd; live blocklist reload. |
| Suffix Trie (not full domain map) | Sub-millisecond lookups; memory-efficient at 1M+ domains. |
| HTTP/1.1 only in MITM | h2 complexity + edge cases (frame boundaries, state); most ads over h2 still catchable by domain SNI layer. |
| TCP pinning for path rules on QUIC hosts | QUIC keys not accessible; alternative is to drop all UDP:443 for that host (less ideal UX but guaranteed consistency). |
| CA in Keystore-wrapped AES-GCM | Non-exportable key enforcement; resistant to binary hooks; cert public (shareable for debug). |
| Crash quarantine (AdRulesCrashGuard) | Malformed rule in DB can crash rendering loop; quarantine buys recovery without full rollback. |

## Unresolved Questions

1. **HTTP/3 (QUIC) path filtering viability in P3**: Currently path rules on QUIC hosts fall back to TCP or are dropped. Should P3 consider sidecar HTTP/3 decoder + 1-RTT key schedule integration, or stay with current tradeoff (document + educate users)?

2. **Pinning detection heuristics**: 3-silent-close rule effective in practice? Should include certificate pinning library checks (e.g., TrustKit)?

3. **Memory budget for Trie**: 1M ad domains → ~Xmb Trie. Benchmarked? Any optimization (e.g., string interning for common TLDs)?

4. **Privoxy removal cleanup**: Safe to delete PrivoxyActionFile and cpp/privoxy/ entirely after device testing, or deprecate first?

5. **ECH support timeline**: Low priority for ad-block, but should we log ECH failures for product roadmap visibility?
