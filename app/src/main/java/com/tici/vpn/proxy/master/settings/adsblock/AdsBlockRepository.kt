package com.tici.vpn.proxy.master.settings.adsblock

import android.content.Context
import androidx.room.withTransaction
import com.common.baseui.BaseAppConfig
import com.common.baseui.SharedPrefs
import com.tici.vpn.proxy.master.db.AdRuleDB
import com.tici.vpn.proxy.master.db.VpnDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Deliberately diverges from [com.tici.vpn.proxy.master.settings.appproxy.AppProxyRepository],
 * which does Room I/O on the caller's thread: every call here runs on [Dispatchers.IO].
 * Only ever touches `AdRuleDB` — the split-tunnel table shares this database.
 */
class AdsBlockRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: VpnDatabase,
    private val crashGuard: AdRulesCrashGuard
) : AdsBlockInterface {

    private val dao get() = database.adRuleDao()

    override suspend fun getAll(): List<AdRuleUI> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.mapToUI() }
    }

    override suspend fun getEnabledDomains(): List<String> = withContext(Dispatchers.IO) {
        dao.getEnabledDomains()
    }

    override suspend fun renderBlocklist(): String = withContext(Dispatchers.IO) {
        if (!BaseAppConfig.adsBlock) return@withContext ""
        val domains = when (crashGuard.level) {
            AdRulesCrashGuard.Level.ALL_RULES -> dao.getEnabledDomains()
            AdRulesCrashGuard.Level.DEFAULTS_ONLY -> dao.getEnabledDefaultDomains()
            AdRulesCrashGuard.Level.DISABLED -> return@withContext ""
        }
        val result = BlocklistGenerator.render(domains)
        if (result.droppedCount > 0) {
            Timber.w("Dropped %d invalid ad-block rule(s) at render time", result.droppedCount)
        }
        result.text
    }

    override suspend fun addRule(domain: String, displayName: String): Boolean =
        withContext(Dispatchers.IO) {
            val row = AdRuleDB(
                domain = DefaultAdRulesParser.canonicalize(domain),
                displayName = displayName.trim(),
                createdAt = System.currentTimeMillis()
            )
            val inserted = dao.insert(row) != -1L
            if (inserted) crashGuard.clearQuarantine()
            inserted
        }

    override suspend fun restoreRule(rule: AdRuleUI) = withContext(Dispatchers.IO) {
        dao.insert(
            AdRuleDB(
                domain = rule.domain,
                displayName = rule.displayName,
                enabled = rule.enabled,
                isDefault = rule.isDefault,
                createdAt = System.currentTimeMillis()
            )
        )
        crashGuard.clearQuarantine()
    }

    override suspend fun count(): Int = withContext(Dispatchers.IO) { dao.count() }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
        crashGuard.clearQuarantine()
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.setEnabled(id, enabled)
        crashGuard.clearQuarantine()
    }

    override suspend fun resetToDefaults() = withContext(Dispatchers.IO) {
        val defaults = readDefaultDomains()
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.deleteAll()
            dao.insertAll(defaults.map {
                AdRuleDB(domain = it, displayName = it, isDefault = true, createdAt = now)
            })
        }
        crashGuard.clearQuarantine()
    }

    override suspend fun seedDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        if (SharedPrefs.instance[KEY_AD_RULES_SEEDED, Boolean::class.java, false]) {
            return@withContext
        }
        // count() guard: prefs can be cleared independently of the DB; never clobber user edits.
        if (dao.count() == 0) resetToDefaults()
        // Set only after the insert returned, so a failed seed is retried on next launch.
        SharedPrefs.instance.put(KEY_AD_RULES_SEEDED, true)
    }

    private fun readDefaultDomains(): List<String> {
        return context.assets.open(DEFAULT_RULES_ASSET).bufferedReader().useLines {
            DefaultAdRulesParser.parse(it)
        }
    }

    companion object {
        private const val DEFAULT_RULES_ASSET = "default_ad_rules.txt"
        private const val KEY_AD_RULES_SEEDED = "ad_rules_seeded"
    }
}
