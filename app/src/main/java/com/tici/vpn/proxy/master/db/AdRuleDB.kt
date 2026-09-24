package com.tici.vpn.proxy.master.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One ad-block rule. [domain] is the canonical form (ASCII, lowercase, no leading dot);
 * it is emitted into Privoxy's action file as `.domain` so it covers the host and all subdomains.
 */
@Entity(
    tableName = "AdRuleDB",
    indices = [Index(value = ["domain"], unique = true)]
)
data class AdRuleDB(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "domain")
    val domain: String,

    /** What the user typed (may be an IDN); shown in the UI only. */
    @ColumnInfo(name = "displayName")
    val displayName: String,

    @ColumnInfo(name = "enabled")
    val enabled: Boolean = true,

    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = 0
)
