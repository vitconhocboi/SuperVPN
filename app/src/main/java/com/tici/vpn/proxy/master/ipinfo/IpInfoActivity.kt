package com.tici.vpn.proxy.master.ipinfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import com.core.baseui.BaseActivity
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ActivityIpInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IpInfoActivity : BaseActivity<ActivityIpInfoBinding>() {

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun bindingProvider(inflater: LayoutInflater): ActivityIpInfoBinding {
        return ActivityIpInfoBinding.inflate(inflater)
    }

    @SuppressLint("CommitTransaction")
    override fun initViews(savedInstanceState: Bundle?) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, IpInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(
                        "proxyConfig", intent.getSerializableExtra("Proxy")
                    )
                }
            }).commit()

    }
}