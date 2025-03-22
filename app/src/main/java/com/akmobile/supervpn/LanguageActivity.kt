package com.akmobile.supervpn

import android.view.LayoutInflater
import com.akmobile.supervpn.databinding.ActivityLanguageBinding
import com.akmobile.supervpn.language.LanguageFragment

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