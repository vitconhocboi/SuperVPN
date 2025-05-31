package com.highsecure.vpn.proxy.master.proxy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface ProxyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(proxy: List<ProxyDB>)

    @Query("SELECT * FROM ProxyDB")
    fun getAll(): List<ProxyDB>

    @Query("SELECT * FROM ProxyDB WHERE active = 1 LIMIT 1")
    fun findActiveProxy(): ProxyDB?

    @Update
    fun update(proxyDB: ProxyDB)

    @Query("SELECT * FROM ProxyDB WHERE id = :id LIMIT 1")
    fun findById(id: String): ProxyDB

    @Query("UPDATE ProxyDB SET active = 0 WHERE active = 1")
    fun resetActive()

    @Query("UPDATE ProxyDB SET active = 1 WHERE id = :id")
    fun setActive(id: String)

    @Transaction
    fun updateActiveProxy(id: String) {
        resetActive()
        setActive(id)
    }
}