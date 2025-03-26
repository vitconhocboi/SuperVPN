package com.akmobile.supervpn

import android.view.LayoutInflater
import com.akmobile.supervpn.databinding.ActivityProxyBinding
import com.akmobile.supervpn.proxy.ProxyFragment
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