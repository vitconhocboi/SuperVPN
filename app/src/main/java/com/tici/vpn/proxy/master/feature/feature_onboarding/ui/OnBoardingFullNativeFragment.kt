package com.tici.vpn.proxy.master.feature.feature_onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.R
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.core.config.domain.data.NativeAdPlace
import com.core.utilities.getStatusBarHeight
import com.core.utilities.padding
import com.tici.vpn.proxy.master.databinding.CoreFragmentOnboardingFullNativeBinding
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFullNativeFragment : BaseFragment<CoreFragmentOnboardingFullNativeBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentOnboardingFullNativeBinding {
        return CoreFragmentOnboardingFullNativeBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.OnBoarding

    companion object {
        fun newInstance() = OnBoardingFullNativeFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fullscreenNativeAd =
            remoteConfigRepository.getAdPlaceBy(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING)
        if (fullscreenNativeAd.isNativeType()) {
            val backgroundColor = (fullscreenNativeAd as? NativeAdPlace)?.backgroundColor?.let {
                try {
                    it.toColorInt()
                } catch (e: Exception) {
                    ContextCompat.getColor(requireContext(), R.color.intro_blue)
                }
            } ?: ContextCompat.getColor(requireContext(), R.color.intro_blue)

            binding.layoutRoot.setBackgroundColor(backgroundColor) // đặt màu nền giống màu nền quảng cáo
        }

        binding.layoutRoot.padding(top = getStatusBarHeight()) // Fullscreen cách statusbar (để hiển thị chữ "i" quảng cáo không bị che)
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(adResource, CoreAdPlaceName.ANCHORED_FULL_ONBOARDING)
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(CoreAdPlaceName.ANCHORED_FULL_ONBOARDING)
    }

}