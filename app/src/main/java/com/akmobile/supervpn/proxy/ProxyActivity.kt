package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import com.akmobile.supervpn.R
import com.akmobile.supervpn.base.ProductActivity
import com.akmobile.supervpn.databinding.ActivityProxyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProxyActivity : ProductActivity<ActivityProxyBinding>() {
    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProxyFragment())
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityProxyBinding {
        return ActivityProxyBinding.inflate(inflater)
    }
}