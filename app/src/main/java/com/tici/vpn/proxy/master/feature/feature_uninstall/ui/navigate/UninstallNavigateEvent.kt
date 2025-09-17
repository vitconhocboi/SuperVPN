package com.tici.vpn.proxy.master.feature.feature_uninstall.ui.navigate

import com.core.baseui.navigator.NavigatorEvent

sealed class UninstallNavigateEvent : NavigatorEvent {
    object OpenUninstallScreen: UninstallNavigateEvent()
    object BackEvent: UninstallNavigateEvent()
    object OpenUninstallFeedbackScreen: UninstallNavigateEvent()
}