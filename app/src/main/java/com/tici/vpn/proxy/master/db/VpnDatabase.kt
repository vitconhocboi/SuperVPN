package com.tici.vpn.proxy.master.db

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(
    entities = [VpnAppItemDB::class],
    version = 2,
    exportSchema = true
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun appVpnDao(): AppVpnDao
}