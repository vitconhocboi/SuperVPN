package com.tici.vpn.proxy.master.di

import com.tici.vpn.proxy.master.settings.appproxy.AppProxyInterface
import com.tici.vpn.proxy.master.settings.appproxy.AppProxyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppProxyDI {
    @Provides
    @Singleton
    fun provideAppProxyRepository(repository: AppProxyRepository): AppProxyInterface {
        return repository
    }
}