package com.tici.vpn.proxy.master.feature.feature_onboarding.ui

import com.core.baseui.navigator.NavigatorEvent

sealed class OnBoardingEvent: NavigatorEvent {
    object BackEvent: OnBoardingEvent()
    object NextEvent: OnBoardingEvent()
    object FinishStep: OnBoardingEvent()
}