package com.tici.vpn.proxy.master.core.base_ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.core.baseui.BaseBottomSheetDialogFragment
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.tici.vpn.proxy.master.databinding.DialogExitBinding
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogExit : BaseBottomSheetDialogFragment<DialogExitBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogExitBinding {
        return DialogExitBinding.inflate(inflater, container, false)
    }

    var onExit: (() -> Unit)? = null


    override fun initView() {
        viewBinding.run {
            buttonNo.setOnClickListener {
                dismiss()
            }

            buttonYes.setOnClickListener {
                onExit?.invoke()
            }
        }
    }

    fun setBannerNativeAd(adResource: AdLoadBannerNativeUiResource) {
        viewBinding.layoutBannerNative.processAdResource(adResource, AppAdPlaceName.ANCHORED_EXIT)
    }
}