package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper

import com.core.config.domain.data.AppConfig
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingActivity
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2.OnBoardingActivity2
import com.tici.vpn.proxy.master.required.OnBoardingConfig

object OnBoardingConfigFactory {

    const val INTRO_PAGE_COUNT = 3


    fun getOnBoardingAdPlaceName(
        onBoardingConfig: OnBoardingConfig,
        appConfig: AppConfig
    ): List<IAdPlaceName> {
        return if (onBoardingConfig.version == OnBoardingConfig.ONBOARDING_VERSION_1) {
            mutableListOf<IAdPlaceName>().apply {
                add(CoreAdPlaceName.ANCHORED_ONBOARDING_BOTTOM)
                if (appConfig.introData.contains(AppConfig.DEFINE_INTRO_FULL_AD)) {
                    add(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING)
                }
            }
        } else {
            mutableListOf<IAdPlaceName>().apply {
                add(CoreAdPlaceName.ANCHORED_ONBOARDING_BOTTOM_v2)
                if (appConfig.introDataV2.contains(AppConfig.DEFINE_INTRO_FULL_AD)) {
                    add(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING_v2)
                }
            }
        }
    }

    fun getOnBoardingAnchorFullAdPlaceName(onBoardingConfig: OnBoardingConfig): IAdPlaceName {
        return if (onBoardingConfig.version == OnBoardingConfig.ONBOARDING_VERSION_1) {
            CoreAdPlaceName.ANCHORED_FULL_ONBOARDING
        } else {
            CoreAdPlaceName.ANCHORED_FULL_ONBOARDING_v2
        }
    }

    fun getOnBoardingClass(onBoardingConfig: OnBoardingConfig) =
        if (onBoardingConfig.version == OnBoardingConfig.ONBOARDING_VERSION_1) {
            OnBoardingActivity::class.java
        } else {
            OnBoardingActivity2::class.java
        }

    fun getImageResIntro(position: Int): Int {
        return when (position) {
            0 -> R.drawable.guide1
            1 -> R.drawable.guide2
            2 -> R.drawable.guide3
            else -> R.drawable.guide1
        }
    }

    fun getStringIntro(position: Int): Int {
        return when (position) {
            0 -> R.string.core_onboarding_title_1
            1 -> R.string.core_onboarding_title_2
            2 -> R.string.core_onboarding_title_3
            else -> R.string.core_onboarding_title_1
        }
    }

    fun getSubtitleIntro(position: Int): Int? {
        return null
    }

}