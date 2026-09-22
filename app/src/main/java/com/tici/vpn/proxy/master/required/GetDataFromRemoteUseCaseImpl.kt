package com.tici.vpn.proxy.master.required

import com.core.config.data.RemoteConfigService
import com.core.config.domain.GetDataFromRemoteConfigUseCase
import com.core.utilities.util.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDataFromRemoteUseCaseImpl @Inject constructor(): GetDataFromRemoteConfigUseCase {
    var onBoardingConfig: OnBoardingConfig = OnBoardingConfig()

    override fun invoke(remoteConfig: RemoteConfigService) {
        //TODO get data from remote config
        val applicationConfig = remoteConfig.fetchOtherConfig<String>("application_config")
        Timber.Forest.d("applicationConfig: $applicationConfig")

        val onBoardingConfigModel = remoteConfig.fetchOtherConfig<OnBoardingConfigModel>("onboarding_config")
        onBoardingConfig = OnBoardingConfig(
            onBoardingConfigModel?.version ?: OnBoardingConfig.ONBOARDING_VERSION_1
        )
    }
}