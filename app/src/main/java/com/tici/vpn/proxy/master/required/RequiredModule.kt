package com.tici.vpn.proxy.master.required

import com.tici.vpn.proxy.master.required.ads.GetDataFromRemoteUseCaseImpl
import com.tici.vpn.proxy.master.required.ads.ProviderAppProviderAdPlaceName
import com.tici.vpn.proxy.master.required.inapp.ProductIdProviderImpl
import com.core.billing.ProductIdProvider
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import com.core.config.domain.data.IAppProviderAdPlaceName
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

    @Provides
    @Singleton
    fun providerProviderAppProviderAdPlaceName(provider: ProviderAppProviderAdPlaceName): IAppProviderAdPlaceName= provider

}