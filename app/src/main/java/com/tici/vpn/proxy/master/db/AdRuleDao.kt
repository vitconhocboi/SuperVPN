package com.tici.vpn.proxy.master.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Unlike [AppVpnDao], every call here is `suspend` so Room runs it off the main thread.
 * Duplicate domains are ignored by the unique index: [insert] returns -1 in that case.
 */
@Dao
interface AdRuleDao {

    @Query("SELECT * FROM AdRuleDB ORDER BY isDefault ASC, createdAt DESC, domain ASC")
    suspend fun getAll(): List<AdRuleDB>

    @Query("SELECT domain FROM AdRuleDB WHERE enabled = 1 ORDER BY domain ASC")
    suspend fun getEnabledDomains(): List<String>

    @Query("SELECT domain FROM AdRuleDB WHERE enabled = 1 AND isDefault = 1 ORDER BY domain ASC")
    suspend fun getEnabledDefaultDomains(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: AdRuleDB): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<AdRuleDB>)

    @Query("DELETE FROM AdRuleDB WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE AdRuleDB SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM AdRuleDB")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM AdRuleDB")
    suspend fun count(): Int
}
