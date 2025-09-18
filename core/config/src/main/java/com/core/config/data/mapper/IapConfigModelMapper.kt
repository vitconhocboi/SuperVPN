package com.core.config.data.mapper

import com.core.config.data.model.IapConfigModel
import com.core.config.domain.data.IapConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IapConfigModelMapper @Inject constructor() : ModelMapper<IapConfigModel, IapConfig> {

    override fun toData(model: IapConfigModel): IapConfig {
        return IapConfig(
            isShowIAPOnStart = model.isShowIAPOnStart ?: false,
            isShowIAPFirstOpen = model.isShowIAPFirstOpen ?: true,
            isShowIAPBeforeRequestPermission = model.isShowIAPBeforeRequestPermission ?: true,
            isEnableIapV2 = model.isEnableIapV2 ?: true,
            timeWaitToShowCloseIcon = model.timeWaitToShowCloseIcon ?: 1500,
            upgradePremiumDisableByCountry = model.upgradePremiumDisableByCountry ?: listOf()
        )
    }
}