package com.core.analytics.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.core.analytics.AnalyticsManager
import com.core.analytics.AnalyticsManagerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    abstract fun bindAnalyticsManager(analyticsManager: AnalyticsManagerImpl): AnalyticsManager

}