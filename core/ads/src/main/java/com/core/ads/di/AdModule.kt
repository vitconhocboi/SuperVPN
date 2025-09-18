package com.core.ads.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.core.ads.admob.AdmobManager
import com.core.ads.domain.AdsManager

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {

    @Binds
    abstract fun bindAdsManager(adsManager: AdmobManager): AdsManager

}