package com.tici.vpn.proxy.master.settings.adsblock

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash breadcrumb around Privoxy start. A malformed action file makes Privoxy `exit(1)` the whole
 * app process, so failure can only be detected on the NEXT launch: a start marked pending but
 * never cleared means it died.
 *
 * Each detected crash degrades one level: all rules → default rules only → no rules, so the user
 * can always reconnect. Editing rules resets to [Level.ALL_RULES].
 *
 * Own prefs file written with `commit()` — the shared `SharedPrefs.put` uses `apply()`, which may
 * not reach disk before `exit(1)`.
 */
@Singleton
class AdRulesCrashGuard @Inject constructor(@ApplicationContext context: Context) {

    enum class Level { ALL_RULES, DEFAULTS_ONLY, DISABLED }

    private val prefs = context.getSharedPreferences("ad_rules_crash_guard", Context.MODE_PRIVATE)

    val level: Level
        get() = Level.entries.getOrElse(prefs.getInt(KEY_LEVEL, 0)) { Level.DISABLED }

    /** True while user rules are turned off after a crash; phase-04 UI shows a banner. */
    val isQuarantined: Boolean get() = level != Level.ALL_RULES

    /** Call once per process start, before any VPN start. */
    @SuppressLint("ApplySharedPref")
    fun onAppLaunch() {
        if (!prefs.getBoolean(KEY_START_PENDING, false)) return
        val next = Level.entries[minOf(level.ordinal + 1, Level.DISABLED.ordinal)]
        prefs.edit().putInt(KEY_LEVEL, next.ordinal).putBoolean(KEY_START_PENDING, false).commit()
        Timber.e("Previous filter start died; ad rules degraded to %s", next)
    }

    /** Synchronous: must be on disk before `nativeStart`, which may kill the process. */
    @SuppressLint("ApplySharedPref")
    fun markStartPending() {
        prefs.edit().putBoolean(KEY_START_PENDING, true).commit()
    }

    fun clearStartPending() {
        prefs.edit().putBoolean(KEY_START_PENDING, false).apply()
    }

    /** User changed the rule list — give it another chance. */
    fun clearQuarantine() {
        if (isQuarantined) prefs.edit().putInt(KEY_LEVEL, Level.ALL_RULES.ordinal).apply()
    }

    private companion object {
        const val KEY_START_PENDING = "ad_rules_start_pending"
        const val KEY_LEVEL = "ad_rules_quarantine_level"
    }
}
