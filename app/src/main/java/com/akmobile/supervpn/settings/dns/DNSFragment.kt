package com.akmobile.supervpn.settings.dns

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentDnsBinding
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setVisible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DNSFragment : ProductFragment<FragmentDnsBinding>() {
    private val mDnsModel: DNSViewModel by viewModels()

    @Inject
    lateinit var mPagerAdapter: DnsAdapter

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDnsBinding {
        return FragmentDnsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
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
                val dns = mDnsModel.allDNS.value?.filter { it.active }?.map { it.server }
                if (dns != null) {
                    BaseAppConfig.dnsServer = dns.joinToString(",")
                } else {
                    BaseAppConfig.dnsServer = ""
                }
            }
        }
    }
}