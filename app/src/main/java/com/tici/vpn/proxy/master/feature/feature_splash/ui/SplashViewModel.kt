package com.tici.vpn.proxy.master.feature.feature_splash.ui

import androidx.lifecycle.ViewModel
import com.core.preference.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    appPreferences: AppPreferences
) : ViewModel() {

    val showRequireTurnOnNetworkWhenRetryClickedFlow = MutableSharedFlow<Boolean>()

    var needHandleEventWhenResume = false

    var isActivityResume = false

    init {
        appPreferences.openAppCount++
    }
}