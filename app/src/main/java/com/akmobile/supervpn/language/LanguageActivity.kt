package com.akmobile.supervpn.language

import android.view.LayoutInflater
import com.akmobile.supervpn.base.ProductActivity
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.ActivityLanguageBinding

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