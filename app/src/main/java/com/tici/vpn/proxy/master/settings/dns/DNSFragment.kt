package com.tici.vpn.proxy.master.settings.dns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.common.baseui.extension.setVisible
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentDnsBinding
import com.tici.vpn.proxy.master.dialog.SettingSuccessDialog
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DNSFragment : BaseFragment<FragmentDnsBinding>() {
    private val mDnsModel: DNSViewModel by viewModels()

    @Inject
    lateinit var mPagerAdapter: DnsAdapter

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDnsBinding {
        return FragmentDnsBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.DNSFragment

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_SETTINGS
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_SETTINGS
        )
    }

    var isChange : Boolean = false

    var isNeedReload = false

    override fun initViews(savedInstanceState: Bundle?) {
        with(binding) {
            rcvDns.adapter = mPagerAdapter
            mDnsModel.getAllProxy(requireContext())

            mDnsModel.allDNS.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    mPagerAdapter.updateData(it)
                }
                progress.setVisible(false)
            }

            btnSave.setOnClickListener {
                mDnsModel.saveDnsSetting(mPagerAdapter.datas)
                SettingSuccessDialog(R.string.setting_dns_set).show(
                    childFragmentManager,
                    "SettingSuccessDialog"
                )
            }

//            btnSave.setOnClickListener {
//                val dns = mDnsModel.allDNS.value?.filter { it.active }?.map { it.server }
//                if (dns?.isNotEmpty() == true) {
//                    BaseAppConfig.dnsServer = dns.joinToString(",")
//                } else {
//                    BaseAppConfig.dnsServer = ""
//                }
//                Navigator.startMainActivity(requireContext(), "RECONNECT")
//            }
        }

        mDnsModel.isLoading.observe(this) { it ->
            if (it == true) {
                binding.progress.setVisible(true)
            } else {
                binding.progress.setVisible(false)
            }
        }

        mDnsModel.errorMessage.observe(this) { it ->
            if (it.isNotEmpty()) {
                Toast.makeText(requireContext(), "internet connection error.", Toast.LENGTH_SHORT).show()
                onBackPress()
            }
        }

        isNeedReload = true
    }

    fun isDnsChanged() : Boolean {
        return isChange
    }

    fun loadData() {
        mDnsModel.getAllProxy(requireContext())
    }
}