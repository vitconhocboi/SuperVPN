package com.highsecure.vpn.proxy

import android.view.LayoutInflater
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivityDnsBinding
import com.highsecure.vpn.proxy.master.proxy.FreeProxyFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DNSActivity : ProductActivity<ActivityDnsBinding>() {
    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
            FreeProxyFragment()
        )
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityDnsBinding {
        return ActivityDnsBinding.inflate(inflater)
    }
}