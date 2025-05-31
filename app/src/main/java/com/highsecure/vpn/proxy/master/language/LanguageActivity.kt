package com.highsecure.vpn.proxy.master.language

import android.view.LayoutInflater
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.databinding.ActivityLanguageBinding

class LanguageActivity : ProductActivity<ActivityLanguageBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }

    override fun initView() {
        val isFromSetting = intent.getBooleanExtra("fromSetting", false)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LanguageFragment(fromSetting = isFromSetting))
            .commit()
    }

}