package com.tici.vpn.proxy.master.language

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.lingver.Lingver
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.DialogAgreementBinding
import com.tici.vpn.proxy.master.splash.CloseAgreementDialog
import com.tici.vpn.proxy.master.utils.Navigator
import java.util.Locale


class DataPrivacyConsentFragment : ProductFragment<DialogAgreementBinding>() {

    var onContinue: (() -> Unit)? = null

    companion object {
        fun newInstance(): DataPrivacyConsentFragment {
            return DataPrivacyConsentFragment()
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogAgreementBinding {
        return DialogAgreementBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        binding.apply {
            btnAccept.setOnClickListener {
                onContinue?.let { it1 -> it1() }
            }

            btnClose.setOnClickListener {
                CloseAgreementDialog(activity = requireActivity()).show()
            }
        }
    }
}