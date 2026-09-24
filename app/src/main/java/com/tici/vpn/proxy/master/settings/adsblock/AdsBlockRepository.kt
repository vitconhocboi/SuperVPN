package com.tici.vpn.proxy.master.settings.adsblock

import android.content.Context
import androidx.room.withTransaction
import com.common.baseui.SharedPrefs
import com.tici.vpn.proxy.master.db.AdRuleDB
import com.tici.vpn.proxy.master.db.VpnDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Deliberately diverges from [com.tici.vpn.proxy.master.settings.appproxy.AppProxyRepository],
 * which does Room I/O on the caller's thread: every call here runs on [Dispatchers.IO].
 * Only ever touches `AdRuleDB` — the split-tunnel table shares this database.
 */
class AdsBlockRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: VpnDatabase
) : AdsBlockInterface {

    private val dao get() = database.adRuleDao()

    override suspend fun getAll(): List<AdRuleUI> = withContext(Dispatchers.IO) {
        dao.getAll().map { it.mapToUI() }
    }

    override suspend fun getEnabledDomains(): List<String> = withContext(Dispatchers.IO) {
        dao.getEnabledDomains()
    }

    override suspend fun addRule(domain: String, displayName: String): Boolean =
        withContext(Dispatchers.IO) {
            val row = AdRuleDB(
                domain = DefaultAdRulesParser.canonicalize(domain),
                displayName = displayName.trim(),
                createdAt = System.currentTimeMillis()
            )
            dao.insert(row) != -1L
        }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.setEnabled(id, enabled)
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
