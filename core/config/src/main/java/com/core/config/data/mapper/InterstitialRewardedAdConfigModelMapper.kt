package com.core.config.data.mapper

import com.core.config.data.helper.ConfigParam
import com.core.config.data.model.RewardedInterstitialAdConfigModel
import com.core.config.domain.data.RewardedInterstitialAdTypeConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class InterstitialRewardedAdConfigModelMapper @Inject constructor(
): ModelMapper<RewardedInterstitialAdConfigModel, RewardedInterstitialAdTypeConfig> {

    override fun toData(model: RewardedInterstitialAdConfigModel): RewardedInterstitialAdTypeConfig {
        return RewardedInterstitialAdTypeConfig(
            isWaitLoadToShow = model.isWaitLoadToShow ?: false,
            isEnableRetry = model.isEnableRetry ?: ConfigParam.RETRY_IS_ENABLE_RETRY,
            maxRetryCount = model.maxRetryCount ?: ConfigParam.RETRY_MAX_RETRY_COUNT,
            retryIntervalSecondList = model.retryIntervalSecondList ?: ConfigParam.RETRY_INTERVAL_LIST,
        )
    }

}