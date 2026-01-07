package com.tici.vpn.proxy.master

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.BaseAdsBottomSheetDialogFragment
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectConfirmBinding
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisconnectConfirmDialog(val onConfirm: () -> Unit) :
    BaseAdsBottomSheetDialogFragment<FragmentDisconnectConfirmBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDisconnectConfirmBinding {
        return FragmentDisconnectConfirmBinding.inflate(inflater, container, false)
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_TOP_CONFIRM_DISCONNECT
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_TOP_CONFIRM_DISCONNECT
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvCancel.setOnClickListener {
                dismiss()
            }
            tvConfirm.setOnClickListener {
                showInterAd(AppAdPlaceName.FULLSCREEN_SELECTED_PROXY_HOME) {
                    onConfirm()
                    dismiss()
                }
            }
        }
    }

}