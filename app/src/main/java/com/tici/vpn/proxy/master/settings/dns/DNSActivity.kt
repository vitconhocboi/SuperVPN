package com.tici.vpn.proxy

import android.view.LayoutInflater
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductActivity
import com.tici.vpn.proxy.master.databinding.ActivityDnsBinding
import com.tici.vpn.proxy.master.proxy.FreeProxyFragment
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