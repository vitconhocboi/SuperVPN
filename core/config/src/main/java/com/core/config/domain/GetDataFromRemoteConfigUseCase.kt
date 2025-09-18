package com.core.config.domain

import com.core.config.data.RemoteConfigService

interface GetDataFromRemoteConfigUseCase {
    operator fun invoke(remoteConfig: RemoteConfigService)
}