package com.tici.vpn.proxy.master.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VpnAppItemDB")
class VpnAppItemDB (
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "packageName")
    val packageName: String
)