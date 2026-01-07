package com.tici.vpn.proxy.master.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.context
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.IAdPlaceName
import com.core.rate.RateInApp
import com.core.utilities.util.Timber
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentConnectedBinding
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.preferences.CoreAppPreferences
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectedFragment : BaseFragment<FragmentConnectedBinding>() {

    override val screenType: ScreenType
        get() = AppScreenType.ConnectedFragment

    private var currentProxy: ProxySpeedTest.ProxyConfig? = null
    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentConnectedBinding {
        return FragmentConnectedBinding.inflate(inflater, container, false)
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_CONNECTED_SUCCESS
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_CONNECTED_SUCCESS
        )
    }

    override fun initViews(savedInstanceState: Bundle?) {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        binding.apply {
            ivIpInfo.scaleX = if (isRTL) -1f else 1f
            pnIpInfo.setOnClickListener {
                Navigator.startProxyInfoActivity(requireActivity(), currentProxy)
            }
            if (BaseAppConfig.proxy.isNotEmpty()) {
                ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
                tvCountry.text = requireContext().safeGetString(BaseAppConfig.proxyCountry)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                tvCountry.typeface = typeface
                tvCountry.setTextColor(resources.getColor(R.color.language_item_text_color))
            } else {
                ivFlag.setImageResource(R.drawable.ic_earth)
                tvCountry.text = getString(R.string.ip_proxy)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
                tvCountry.typeface = typeface
                tvCountry.setTextColor(resources.getColor(R.color.green_1))
            }
        }
    }

    fun Context.safeGetString(resourceName: String): String? {
        val resId = resources.getIdentifier(resourceName, "string", packageName)
        return if (resId != 0) getString(resId) else null
    }

//    override fun getTitle(): String {
//        return try {
//            resources.getString(R.string.connect_success)
//        } catch (e: Exception) {
//            resources.getString(R.string.connect_success)
//        }
//    }

    fun setCurrentProxy(proxy: ProxySpeedTest.ProxyConfig?) {
        currentProxy = proxy
    }
}