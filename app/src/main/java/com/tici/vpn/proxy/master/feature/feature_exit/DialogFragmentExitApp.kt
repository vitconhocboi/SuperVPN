package com.tici.vpn.proxy.master.feature.feature_exit

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.BaseAdsDialogFragment
import com.core.config.domain.data.IAdPlaceName
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.databinding.FragmentDialogExitAppBinding
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogFragmentExitApp :
    BaseAdsDialogFragment<FragmentDialogExitAppBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDialogExitAppBinding {
        return FragmentDialogExitAppBinding.inflate(inflater, container, false)
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_EXIT
        )
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        viewBinding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_EXIT
        )
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.tvNoExit.setOnSingleClick {
            dismiss()
        }

        viewBinding.tvExitApp.setOnSingleClick {
            (activity as? MainActivity)?.finish()
        }
    }

    override fun onResume() {
        val window: Window = dialog?.window!!
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val size = Point()
        val display: Display = window.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.BOTTOM)
        super.onResume()
    }

}