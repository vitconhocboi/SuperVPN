package com.tici.vpn.proxy.master.settings.dns

import android.os.Bundle
import android.view.LayoutInflater
import com.core.baseui.BaseActivity
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ActivityDnsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DNSActivity : BaseActivity<ActivityDnsBinding>() {

    override fun initViews(savedInstanceState: Bundle?) {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            DNSFragment()
        )
            .commit()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityDnsBinding {
        return ActivityDnsBinding.inflate(inflater)
    }
}