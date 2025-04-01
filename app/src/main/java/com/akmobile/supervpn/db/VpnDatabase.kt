package com.akmobile.supervpn.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akmobile.supervpn.proxy.ProxyDAO
import com.akmobile.supervpn.proxy.ProxyDB

@Database(
    entities = [VpnAppItemDB::class , ProxyDB::class],
    version = 1,
    exportSchema = true
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun createAppVpnDao(): AppVpnDao

    abstract fun proxyDAO(): ProxyDAO
}