package com.tici.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.tici.vpn.proxy.master.base.ProductActivity
import com.tici.vpn.proxy.master.databinding.ActivityPremiumBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumActivity : ProductActivity<ActivityPremiumBinding>() {

    override fun bindingProvider(inflater: LayoutInflater): ActivityPremiumBinding {
        return ActivityPremiumBinding.inflate(inflater)
    }

    @SuppressLint("CommitTransaction")
    override fun initView() {
        supportFragmentManager.beginTransaction()
            .replace(com.tici.vpn.proxy.master.R.id.fragment_container_pre, PremiumFragment())
            .commit()
    }
}