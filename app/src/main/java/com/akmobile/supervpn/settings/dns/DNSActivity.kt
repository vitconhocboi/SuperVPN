package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import com.akmobile.supervpn.R
import com.akmobile.supervpn.base.ProductActivity
import com.akmobile.supervpn.databinding.ActivityDnsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DNSActivity : ProductActivity<ActivityDnsBinding>() {
    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FreeProxyFragment())
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityDnsBinding {
        return ActivityDnsBinding.inflate(inflater)
    }
}