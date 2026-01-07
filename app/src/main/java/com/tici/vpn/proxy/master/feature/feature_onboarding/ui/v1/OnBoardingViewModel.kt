package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1

import androidx.lifecycle.SavedStateHandle
import com.core.baseui.BaseSharedViewModel
import com.core.config.domain.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    remoteConfigRepository: RemoteConfigRepository,
    handle: SavedStateHandle,
) : BaseSharedViewModel<OnBoardingEvent>(
    remoteConfigRepository, handle
) {
    override fun navigateActionBack() {
        navigateTo(OnBoardingEvent.BackEvent)
    }
}