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
