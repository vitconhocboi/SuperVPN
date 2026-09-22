package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper

import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingActivity
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2.OnBoardingActivity2
import com.tici.vpn.proxy.master.required.OnBoardingConfig

object OnBoardingConfigFactory {

    const val INTRO_PAGE_COUNT = 3


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