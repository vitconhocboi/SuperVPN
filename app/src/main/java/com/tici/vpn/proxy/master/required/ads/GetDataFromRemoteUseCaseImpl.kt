package com.tici.vpn.proxy.master.required.ads

import com.core.config.data.RemoteConfigService
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import com.core.utilities.util.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDataFromRemoteUseCaseImpl @Inject constructor(): GetDataFromRemoteConfigUseCase {
    override fun invoke(remoteConfig: RemoteConfigService) {
        //TODO get data from remote config
        val applicationConfig = remoteConfig.fetchOtherConfig<String>("application_config")
        Timber.Forest.d("applicationConfig: $applicationConfig")
    }
}