package com.tici.vpn.proxy.master.feature.feature_uninstall.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.core.baseui.InsetsViewModel
import com.core.baseui.fragment.BaseChildOfHostFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.databinding.CoreFragmentUninstallBinding
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.navigate.UninstallNavigateEvent
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UninstallChildOfHostFragment :
    BaseChildOfHostFragment<CoreFragmentUninstallBinding, UninstallNavigateEvent, UninstallShareViewModel>() {
    private val insetsViewModel: InsetsViewModel by activityViewModels()
    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): CoreFragmentUninstallBinding {
        return CoreFragmentUninstallBinding.inflate(inflater, container, false)
    }

    override val hostViewModel: UninstallShareViewModel by viewModels(ownerProducer = { requireParentFragment() })
    override val screenType: ScreenType
        get() = AppScreenType.Uninstall

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        binding.run {
            tvStillUninstall.setOnSingleClick {
                hostViewModel.navigateTo(event = UninstallNavigateEvent.OpenUninstallFeedbackScreen)
            }

            btnBack.setOnSingleClick {
                hostViewModel.navigateActionBack()
            }

            tvDontWantUninstall.setOnSingleClick {
                hostViewModel.navigateActionBack()
            }

            bgClNoLongInteresting.setOnSingleClick {
                hostViewModel.navigateActionBack()
            }

            bgClUnableConnectVPN.setOnSingleClick {
                hostViewModel.navigateActionBack()
            }

        }

        collectFlowOn(insetsViewModel.systemInsets) { systemInsets ->
            systemInsets?.insets?.let {
                binding.root.setPadding(
                    it.left,
                    it.top,
                    it.right,
                    it.bottom
                )
            }
        }
    }

}