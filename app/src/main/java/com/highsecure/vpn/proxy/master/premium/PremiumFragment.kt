package com.highsecure.vpn.proxy.master.premium

import android.view.LayoutInflater
import android.view.ViewGroup
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.FragmentPremiumBinding

class PremiumFragment : ProductFragment<FragmentPremiumBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.apply {
            btnClose.setOnClickListener {
                (activity as? PremiumActivity)?.apply {
                    finish()
                }
            }
        }
    }
}