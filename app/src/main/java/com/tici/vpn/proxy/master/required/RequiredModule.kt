package com.tici.vpn.proxy.master.required

import com.tici.vpn.proxy.master.required.inapp.ProductIdProviderImpl
import com.core.billing.ProductIdProvider
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RequiredModule {
    @Provides
    @Singleton
    fun providerProductIds(provider: ProductIdProviderImpl): ProductIdProvider = provider

    @Provides
    @Singleton
    fun providerGetDataFromRemoteUseCase(useCase: GetDataFromRemoteUseCaseImpl): GetDataFromRemoteConfigUseCase = useCase

}