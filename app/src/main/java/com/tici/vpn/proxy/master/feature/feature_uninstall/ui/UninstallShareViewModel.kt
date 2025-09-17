package com.tici.vpn.proxy.master.feature.feature_uninstall.ui

import androidx.lifecycle.SavedStateHandle
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.navigate.UninstallNavigateEvent
import com.core.baseui.BaseSharedViewModel
import com.core.config.domain.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UninstallShareViewModel @Inject constructor(
    remoteConfigRepository: RemoteConfigRepository,
    handle: SavedStateHandle
): BaseSharedViewModel<UninstallNavigateEvent>(remoteConfigRepository, handle) {
    override fun navigateActionBack() {
        navigateTo(UninstallNavigateEvent.BackEvent)
    }
}