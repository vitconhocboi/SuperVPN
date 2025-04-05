package com.akmobile.supervpn.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppVpnDao {

    @Insert
    fun insertAll(items: List<VpnAppItemDB>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VpnAppItemDB)

    @Query("DELETE FROM VpnAppItemDB")
    fun deleteAll()

    @Query("DELETE FROM VpnAppItemDB where packageName = :packageName")
    fun delete(packageName: String)

    @Query("SELECT * FROM VpnAppItemDB")
    fun getAll(): List<VpnAppItemDB>
}