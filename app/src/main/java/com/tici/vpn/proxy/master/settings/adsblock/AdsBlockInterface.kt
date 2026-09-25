package com.tici.vpn.proxy.master.settings.adsblock

/** Ad-block rule list. All functions are main-safe (they switch to IO internally). */
interface AdsBlockInterface {
    suspend fun getAll(): List<AdRuleUI>

    /** Canonical domains of enabled rules, for the Privoxy action file. */
    suspend fun getEnabledDomains(): List<String>

    /**
     * Body for Privoxy's `default.action`: empty when ad-block is off (DB not touched); honours the
     * crash quarantine level; every domain passes [DomainValidator] again (gate 2).
     */
    suspend fun renderActionFile(): String

    /** @return false if [domain] already exists (unique index). */
    suspend fun addRule(domain: String, displayName: String): Boolean

    /** Undo of [delete]: re-inserts [rule] keeping its domain, enabled and isDefault flags. */
    suspend fun restoreRule(rule: AdRuleUI)

    suspend fun count(): Int

    suspend fun delete(id: Long)

    suspend fun setEnabled(id: Long, enabled: Boolean)

    /** Wipes every rule, user-added ones included, and reseeds the shipped defaults. */
    suspend fun resetToDefaults()

    /** One-time seed on first launch after install or upgrade; no-op afterwards. */
    suspend fun seedDefaultsIfNeeded()
}
