package com.simple.libads.manager

import android.app.Activity
import android.content.Context
import com.simple.libads.base.OpenAdLoader

class EmptyOpenLoader: OpenAdLoader() {
    override fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(activity, object : OnShowAdCompleteListener {
            override fun onShowAdComplete(isShow: Boolean) {

            }
        })
    }

    override fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener,
    ) {
        onShowAdCompleteListener.onShowAdComplete(false)
    }

    override fun loadAd(context: Context) {
    }
}