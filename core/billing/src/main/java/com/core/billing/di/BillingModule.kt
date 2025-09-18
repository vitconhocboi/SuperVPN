package com.core.billing.di

import com.core.billing.BillingDataSource
import com.core.billing.BillingManager
import com.core.billing.ProductIdManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    abstract fun bindInAppPurchase(billingDataSource: BillingDataSource): BillingManager

    @Binds
    abstract fun bindProductIdPurchase(billingDataSource: BillingDataSource): ProductIdManager
}