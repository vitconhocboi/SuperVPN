package com.akmobile.supervpn

import androidx.viewbinding.ViewBinding
import com.common.baseui.BindingActivity
import com.simple.libads.AdsApplication

abstract class ProductActivity<T : ViewBinding> : BindingActivity<T>() {

    override fun onResume() {
        super.onResume()
        AdsApplication.dialogLoadingBuilder.setMessageInter(getString(R.string.ads_message_inter))
        AdsApplication.dialogLoadingBuilder.setMessageOpen(getString(R.string.ads_message_open))
    }

    override fun isBuyApp(): Boolean {
        return true
    }

    override fun onRequiredOpenSettingPermission(id: Int) {
    }

    override fun showDialogNoInternet(onRetry: () -> Unit) {
    }

    override fun showDialogNoAds() {
    }

    override fun GDPRCheck(onGrant: () -> Unit) {
    }

    override fun dismissDialogLoadingAds() {
    }

    override fun showDialogLoadingAds() {
        dismissDialogLoadingAds()
    }
}