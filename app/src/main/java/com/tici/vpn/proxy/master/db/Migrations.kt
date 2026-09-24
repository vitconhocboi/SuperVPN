package com.tici.vpn.proxy.master.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: drops the remote-proxy server table.
 *
 * The database is shared with split tunneling (`VpnAppItemDB`), so a destructive
 * fallback is not acceptable here — it would wipe the user's per-app allow list.
 * Table name must match the old `@Entity(tableName = "ProxyDB")`.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `ProxyDB`")
    }
}

/**
 * v2 -> v3: adds the ad-block rule table. Additive only — `VpnAppItemDB` is untouched.
 * SQL copied verbatim from the Room-generated `schemas/.../3.json`; do not hand-edit.
 * Rows are seeded later by `AdsBlockRepository.seedDefaultsIfNeeded()`, not here.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `AdRuleDB` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `domain` TEXT NOT NULL, `displayName` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_AdRuleDB_domain` ON `AdRuleDB` (`domain`)")
    }
}
