package com.tici.vpn.proxy.master.db

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(
    entities = [VpnAppItemDB::class, AdRuleDB::class],
    version = 3,
    exportSchema = true
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun appVpnDao(): AppVpnDao

    abstract fun adRuleDao(): AdRuleDao
}
