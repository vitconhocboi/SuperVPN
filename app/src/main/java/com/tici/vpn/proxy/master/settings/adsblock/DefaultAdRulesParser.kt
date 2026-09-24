package com.tici.vpn.proxy.master.settings.adsblock

/**
 * Parses `assets/default_ad_rules.txt`: one domain per line, `#` starts a comment line.
 * Output is canonical (trimmed, lowercase, no leading dot), de-duplicated, in file order.
 *
 * Pure function so it is JVM-unit-testable. It does NOT validate domain syntax — the
 * action-file renderer must re-validate every domain before it can reach Privoxy.
 */
object DefaultAdRulesParser {
    fun parse(lines: Sequence<String>): List<String> {
        return lines
            .map { canonicalize(it) }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .distinct()
            .toList()
    }

    fun canonicalize(raw: String): String {
        return raw.trim().lowercase().trimStart('.')
    }
}
