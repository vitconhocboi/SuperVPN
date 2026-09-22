package com.core.config.domain

import com.core.config.data.FetchRemoteConfigState
import com.core.config.domain.data.AppConfig
import com.core.config.domain.data.IapConfig
import kotlinx.coroutines.flow.SharedFlow


interface RemoteConfigRepository {

    val fetchStateCompleteFlow: SharedFlow<FetchRemoteConfigState>

    fun fetchAndActive()

    fun getAppConfig(): AppConfig

    fun getIapConfig(): IapConfig

}
