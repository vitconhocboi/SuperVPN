package com.core.config.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.core.config.data.RemoteConfigRepositoryImpl
import com.core.config.domain.RemoteConfigRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteConfigRepositoryModule {

    @Binds
    internal abstract fun bindRemoteConfigRepository(remoteConfigRepository: RemoteConfigRepositoryImpl): RemoteConfigRepository

}