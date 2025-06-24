package com.tici.vpn.proxy.master.di

import android.content.Context
import androidx.room.Room
import com.tici.vpn.proxy.master.db.VpnDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VpnDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            VpnDatabase::class.java,
            "vpn_database"
        ).build()
    }
}