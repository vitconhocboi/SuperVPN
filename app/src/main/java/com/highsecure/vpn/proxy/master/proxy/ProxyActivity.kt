package com.highsecure.vpn.proxy.master.proxy

import android.view.LayoutInflater
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivityProxyBinding
import com.highsecure.vpn.proxy.master.proxy.fragments.TabProxyFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProxyActivity : ProductActivity<ActivityProxyBinding>() {
    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
            TabProxyFragment()
        )
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityProxyBinding {
        return ActivityProxyBinding.inflate(inflater)
    }
}