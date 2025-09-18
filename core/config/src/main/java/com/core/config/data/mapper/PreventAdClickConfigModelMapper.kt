package com.core.config.data.mapper

import com.core.config.data.model.PreventAdClickConfigModel
import com.core.config.data.helper.ConfigParam
import com.core.config.domain.data.PreventAdClickConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreventAdClickConfigModelMapper @Inject constructor(
): ModelMapper<PreventAdClickConfigModel, PreventAdClickConfig> {

    override fun toData(model: PreventAdClickConfigModel): PreventAdClickConfig {
        return PreventAdClickConfig(
            maxAdClickPerSession = model.maxAdClickPerSession ?: ConfigParam.PREVENT_AD_CLICK_CONFIG_DEFAULT_MAX_AD_CLICK_PER_SESSION,
            timePerSession = model.timePerSession ?: ConfigParam.PREVENT_AD_CLICK_CONFIG_DEFAULT_TIME_PER_SESSION,
            timeDisableAdsWhenReachedMaxAdClick = model.timeDisableAdsWhenReachedMaxAdClick ?: ConfigParam.PREVENT_AD_CLICK_CONFIG_DEFAULT_TIME_DISABLE,
        )
    }

}