package com.tici.vpn.proxy.master.di

import com.tici.vpn.proxy.master.proxy.ProxyConnection
import com.tici.vpn.proxy.master.proxy.ProxyInterface
import com.tici.vpn.proxy.master.proxy.ProxyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProxyDI {
    @Provides
    @Singleton
    fun provideProxyRepository(repository: ProxyRepository): ProxyInterface {
        return repository
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Provides
    @Singleton
    fun provideProxyUpdate() : ProxyConnection {
        return ProxyConnection()
    }
}