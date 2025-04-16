package com.akmobile.supervpn.premium

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.language.LanguageActivity
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentPremiumBinding
import com.akmobile.supervpn.utils.Navigator

class PremiumFragment : ProductFragment<FragmentPremiumBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.apply {
            btnClose.setOnClickListener {
                (activity as? PremiumActivity)?.apply {
                    finish()
                }
            }
        }
    }
}