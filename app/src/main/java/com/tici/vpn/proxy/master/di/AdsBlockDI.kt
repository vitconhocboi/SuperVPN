package com.tici.vpn.proxy.master.di

import com.tici.vpn.proxy.master.settings.adsblock.AdsBlockInterface
import com.tici.vpn.proxy.master.settings.adsblock.AdsBlockRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AdsBlockDI {
    @Provides
    @Singleton
    fun provideAdsBlockRepository(repository: AdsBlockRepository): AdsBlockInterface {
        return repository
    }
}
