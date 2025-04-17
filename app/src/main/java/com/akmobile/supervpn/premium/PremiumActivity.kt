package com.akmobile.supervpn.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.akmobile.supervpn.base.ProductActivity
import com.akmobile.supervpn.databinding.ActivityPremiumBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumActivity : ProductActivity<ActivityPremiumBinding>() {

    override fun bindingProvider(inflater: LayoutInflater): ActivityPremiumBinding {
        return ActivityPremiumBinding.inflate(inflater)
    }

    @SuppressLint("CommitTransaction")
    override fun initView() {
        supportFragmentManager.beginTransaction()
            .replace(com.akmobile.supervpn.R.id.fragment_container_pre, PremiumFragment())
            .commit()
    }
}