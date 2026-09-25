package com.tici.vpn.proxy.master.settings.adsblock

import java.net.IDN
import java.util.Locale

/**
 * Normalises and validates a domain before it may reach Privoxy's action file.
 *
 * Privoxy runs in-process and `exit(1)`s on a malformed action file, killing the app, so this is
 * an allow-list: a valid result contains only `[a-z0-9.-]`. No wildcards, no paths, no IPs.
 * Pure (no Android deps) — used both when the user adds a rule and again at render time.
 */
object DomainValidator {

    sealed class Result {
        /** [domain] is canonical: ASCII (punycode), lowercase, no leading dot. */
        data class Valid(val domain: String) : Result()
        data class Invalid(val reason: Reason) : Result()
    }

    enum class Reason { EMPTY, WILDCARD, TOO_LONG, INVALID_FORMAT }

    private const val MAX_LENGTH = 253
    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")
    private val PORT_SUFFIX = Regex(":\\d{1,5}$")

    // Labels: 1-63 chars, alnum at both ends. TLD: letters, or a punycode (xn--) label.
    private val DOMAIN = Regex(
        "^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+([a-z]{2,63}|xn--[a-z0-9-]{1,59})$"
    )

    fun validate(raw: String): Result {
        var s = raw.trim().removeSurrounding("\"").removeSurrounding("'").trim()
        if (s.isEmpty()) return Result.Invalid(Reason.EMPTY)
        if (s.contains('*')) return Result.Invalid(Reason.WILDCARD)

        s = s.replaceFirst(SCHEME, "")
        s = s.substringBefore('/')
        s = s.replaceFirst(PORT_SUFFIX, "")
        s = s.removePrefix(".")

        s = try {
            IDN.toASCII(s, IDN.ALLOW_UNASSIGNED)
        } catch (e: IllegalArgumentException) {
            return Result.Invalid(Reason.INVALID_FORMAT)
        }
        s = s.lowercase(Locale.ROOT)

        if (s.isEmpty()) return Result.Invalid(Reason.EMPTY)
        if (s.length > MAX_LENGTH) return Result.Invalid(Reason.TOO_LONG)
        if (!DOMAIN.matches(s)) return Result.Invalid(Reason.INVALID_FORMAT)
        return Result.Valid(s)
    }
}
