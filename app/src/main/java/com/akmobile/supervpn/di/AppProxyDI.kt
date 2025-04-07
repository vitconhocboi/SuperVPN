package com.akmobile.supervpn.di

import com.akmobile.supervpn.settings.appproxy.AppProxyInterface
import com.akmobile.supervpn.settings.appproxy.AppProxyRepository
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