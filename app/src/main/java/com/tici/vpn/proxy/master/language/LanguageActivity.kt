package com.tici.vpn.proxy.master.language

import android.view.LayoutInflater
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.base.ProductActivity
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ActivityLanguageBinding

class LanguageActivity : ProductActivity<ActivityLanguageBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }

    override fun initView() {

        if (!BaseAppConfig.allowCollectData) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DataPrivacyConsentFragment.newInstance().apply {
                    onContinue = {
                        BaseAppConfig.allowCollectData = true
                        val isFromSetting = intent.getBooleanExtra("fromSetting", false)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LanguageFragment.newInstance(fromSetting = isFromSetting))
                            .commit()
                    }
                })
                .commitAllowingStateLoss()
        } else {
            val isFromSetting = intent.getBooleanExtra("fromSetting", false)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LanguageFragment.newInstance(fromSetting = isFromSetting))
                .commit()
        }
    }

}