package com.tici.vpn.proxy.master.settings.adsblock

import java.net.IDN
import java.util.Locale

/**
 * Normalises and validates an ad-block rule before it may reach the engine's blocklist file.
 *
 * A rule is a domain (blocks it and all subdomains) or a domain plus a path prefix
 * (`ads.example.com/banner` — blocked via selective MITM, see engine/mitm.go). This is an
 * allow-list: a valid host contains only `[a-z0-9.-]`, a valid path only URL path characters.
 * No wildcards, no IPs, no query/fragment. The output is one line of the blocklist file, so it
 * can never contain whitespace or newlines. Pure (no Android deps) — used both when the user adds
 * a rule and again at render time.
 */
object DomainValidator {

    sealed class Result {
        /** [domain] is canonical: ASCII (punycode) lowercase host, no leading dot, optional `/path`. */
        data class Valid(val domain: String) : Result()
        data class Invalid(val reason: Reason) : Result()
    }

    enum class Reason { EMPTY, WILDCARD, TOO_LONG, INVALID_FORMAT }

    private const val MAX_LENGTH = 253
    private const val MAX_PATH_LENGTH = 512
    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
    private val PORT_SUFFIX = Regex(":\\d{1,5}$")

    // Labels: 1-63 chars, alnum at both ends. TLD: letters, or a punycode (xn--) label.
    private val DOMAIN = Regex(
        "^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+([a-z]{2,63}|xn--[a-z0-9-]{1,59})$"
    )

    // RFC 3986 path characters (pchar + "/"), percent-encoding allowed.
    private val PATH = Regex("^/[A-Za-z0-9._~!$&'()+,;=:@%/-]*$")

    fun validate(raw: String): Result {
        var s = raw.trim().removeSurrounding("\"").removeSurrounding("'").trim()
        if (s.isEmpty()) return Result.Invalid(Reason.EMPTY)
        if (s.contains('*')) return Result.Invalid(Reason.WILDCARD)

        s = s.replaceFirst(SCHEME, "")
        val slash = s.indexOf('/')
        var host = if (slash >= 0) s.substring(0, slash) else s
        // Query and fragment never reach the engine's path matcher; drop them.
        val path = if (slash >= 0) s.substring(slash).substringBefore('?').substringBefore('#') else ""

        host = host.replaceFirst(PORT_SUFFIX, "").removePrefix(".")
        host = try {
            IDN.toASCII(host, IDN.ALLOW_UNASSIGNED)
        } catch (e: IllegalArgumentException) {
            return Result.Invalid(Reason.INVALID_FORMAT)
        }
        host = host.lowercase(Locale.ROOT)

        if (host.isEmpty()) return Result.Invalid(Reason.EMPTY)
        if (host.length > MAX_LENGTH) return Result.Invalid(Reason.TOO_LONG)
        if (!DOMAIN.matches(host)) return Result.Invalid(Reason.INVALID_FORMAT)

        // A bare "/" means the whole domain.
        if (path.isEmpty() || path == "/") return Result.Valid(host)
        if (path.length > MAX_PATH_LENGTH) return Result.Invalid(Reason.TOO_LONG)
        if (!PATH.matches(path)) return Result.Invalid(Reason.INVALID_FORMAT)
        return Result.Valid(host + path)
    }
}
