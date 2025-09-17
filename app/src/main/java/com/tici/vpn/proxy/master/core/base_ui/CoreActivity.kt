package com.tici.vpn.proxy.master.core.base_ui

import androidx.viewbinding.ViewBinding
import com.core.baseui.RequireTurnOnNetworkBottomSheetFragment
import com.core.baseui.BaseActivity


abstract class CoreActivity<VB : ViewBinding> : BaseActivity<VB>() {

    fun showRequireTurnOnNetworkBottomSheetFragment(onRetry: () -> Unit, onCancel: () -> Unit) {
        RequireTurnOnNetworkBottomSheetFragment().apply {
            this.onRetry = onRetry
            this.onCancel = onCancel
        }.show(
            supportFragmentManager,
            RequireTurnOnNetworkBottomSheetFragment::class.java.simpleName
        )
    }
}