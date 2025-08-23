package com.tici.vpn.proxy.master.ipinfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductActivity
import com.tici.vpn.proxy.master.databinding.ActivityIpInfoBinding

class IpInfoActivity : ProductActivity<ActivityIpInfoBinding>() {

    override fun bindingProvider(inflater: LayoutInflater): ActivityIpInfoBinding {
        return ActivityIpInfoBinding.inflate(inflater)
    }

    @SuppressLint("CommitTransaction")
    override fun initView() {
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