package com.tici.vpn.proxy.master.ipinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentIpInfoBinding

class IpInfoFragment : ProductFragment<FragmentIpInfoBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentIpInfoBinding {
        return FragmentIpInfoBinding.inflate(inflater, container, false)
    }
}