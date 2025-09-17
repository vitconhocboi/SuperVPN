package com.tici.vpn.proxy.master.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.IAdPlaceName
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectedBinding
import com.tici.vpn.proxy.master.extension.safeGetString
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisconnectedFragment : BackActionBarFragment, BaseFragment<FragmentDisconnectedBinding>() {

    var report: ProxyReport? = null
    var countryTitle: String = ""


    override val screenType: ScreenType
        get() = AppScreenType.DisconnectedFragment


    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_CONNECTED_REPORT
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_CONNECTED_REPORT
        )
    }

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.FULLSCREEN_BACK_DISCONNECT
        )
    }

    companion object {
        fun newInstance(
            country: String
        ): DisconnectedFragment {
            val fragment = DisconnectedFragment()
            fragment.countryTitle = country
            return fragment
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDisconnectedBinding {
        return FragmentDisconnectedBinding.inflate(inflater, container, false)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        binding.apply {

            ivFlag.setImageResource(Utils.getFlag(countryTitle))
            titleCountry.text = requireContext().safeGetString(countryTitle)

            tvDuration.text = report?.duration
            tvUpload.text = report?.upload
            tvDownload.text = report?.download
            if (report?.upload?.isEmpty() == true && report?.upload?.isEmpty() == true) {
                ProxySpeedTest.Instance.startSpeedTest(
                    report?.proxyConfig,
                    callback = { download, upload ->
                        try {
                            if (ProxySpeedTest.FAILED != download) tvDownload.text = download
                            if (ProxySpeedTest.FAILED != upload) tvUpload.text = upload
                        } catch (e: Exception) {
                        }
                    })
            }
        }

        binding.tvDoneExit.setOnSingleClick {
            showInterAd(AppAdPlaceName.FULLSCREEN_BACK_DISCONNECT) {
                (activity as? MainActivity)?.navigateHome()
            }
        }
    }

    override fun getTitle(): String {
        return getString(R.string.connect_report)
    }

    fun loadData(report: ProxyReport) {
        this.report = report;
    }
}