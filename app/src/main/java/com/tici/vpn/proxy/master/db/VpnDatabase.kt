package com.tici.vpn.proxy.master.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tici.vpn.proxy.master.proxy.ProxyDAO
import com.tici.vpn.proxy.master.proxy.ProxyDB

@Database(
    entities = [VpnAppItemDB::class , ProxyDB::class],
    version = 1,
    exportSchema = true
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun appVpnDao(): AppVpnDao

    abstract fun proxyDAO(): ProxyDAO
}