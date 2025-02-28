package com.hoangsinh.supervpn

import android.view.LayoutInflater
import com.hoangsinh.supervpn.databinding.ActivityLanguageBinding
import com.hoangsinh.supervpn.language.LanguageFragment

class LanguageActivity : ProductActivity<ActivityLanguageBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }

    override fun initView() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LanguageFragment())
            .commit()
    }

}