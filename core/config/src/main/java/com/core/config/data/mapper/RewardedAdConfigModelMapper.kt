package com.core.config.data.mapper

import com.core.config.data.helper.ConfigParam
import com.core.config.data.model.RewardedAdConfigModel
import com.core.config.domain.data.RewardedAdTypeConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RewardedAdConfigModelMapper @Inject constructor(
): ModelMapper<RewardedAdConfigModel, RewardedAdTypeConfig> {

    override fun toData(model: RewardedAdConfigModel): RewardedAdTypeConfig {
        return RewardedAdTypeConfig(
            isWaitLoadToShow = model.isWaitLoadToShow ?: false,
            isEnableRetry = model.isEnableRetry ?: ConfigParam.RETRY_IS_ENABLE_RETRY,
            maxRetryCount = model.maxRetryCount ?: ConfigParam.RETRY_MAX_RETRY_COUNT,
            retryIntervalSecondList = model.retryIntervalSecondList ?: ConfigParam.RETRY_INTERVAL_LIST,
            maxRetryOnContext = model.maxRetryOnContext ?: ConfigParam.MAX_RETRY_ON_CONTEXT,
            timeWaitRetryOnContext = model.timeWaitRetryOnContext ?: ConfigParam.TIME_WAIT_RETRY_ON_CONTEXT,
        )
    }

}