package com.tici.vpn.proxy.master.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectedBinding
import com.tici.vpn.proxy.master.extension.safeGetString
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.utils.Utils

class DisconnectedFragment : BackActionBarFragment, ProductFragment<FragmentDisconnectedBinding>() {

    var report: ProxyReport? = null
    var countryTitle : String = ""


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

    override fun initView() {
        super.initView()
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
    }

    override fun getTitle(): String {
        return getString(R.string.connect_report)
    }

    fun loadData(report: ProxyReport) {
        this.report = report;
    }
}