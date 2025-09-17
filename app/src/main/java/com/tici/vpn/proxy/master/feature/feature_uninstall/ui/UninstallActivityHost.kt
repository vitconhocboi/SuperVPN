package com.tici.vpn.proxy.master.feature.feature_uninstall.ui

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.navigate.UninstallNavigateEvent
import com.core.baseui.HostBaseActivity
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.databinding.CoreActivityBaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UninstallActivityHost :
    HostBaseActivity<CoreActivityBaseBinding, UninstallNavigateEvent, UninstallShareViewModel>() {

    override val isHideStatusBar: Boolean
        get() = true

    override val isSpaceStatusBar: Boolean
        get() = false

    override val isSpaceDisplayCutout: Boolean
        get() = false

    override fun bindingProvider(inflater: LayoutInflater): CoreActivityBaseBinding {
        return CoreActivityBaseBinding.inflate(inflater)
    }

    override val containerId: Int
        get() = binding.mainContainer.id

    override
    val sharedViewModel: UninstallShareViewModel by viewModels()

    override fun showFirstScreen() {
        replaceFragment(UninstallHostFragment())
    }

    override fun providerPreloadBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_1,
            CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_2
        )
    }
}