package com.akmobile.supervpn.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VpnAppItemDB::class],
    version = 1,
    exportSchema = true
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun createAppVpnDao(): AppVpnDao
}