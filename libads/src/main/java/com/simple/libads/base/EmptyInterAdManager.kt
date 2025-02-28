package com.simple.libads.base

import android.app.Activity
import com.simple.libads.base.interads.InterAdLoader

class EmptyInterAdManager(idAds: String) : InterAdLoader(idAds ) {
    override fun showInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: () -> Unit,
    ) {
        showCallBack.invoke(false)
    }

    override fun loadAndShowInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: () -> Unit,
    ) {
        showCallBack.invoke(false)
    }

    override fun loadInterAds(activity: Activity, onLoader: ((isLoaded: Boolean) -> Unit)?) {
        onLoader?.invoke(false)
    }

    override fun isReady(): Boolean {
        return false
    }

    override fun isLoadFail(): Boolean {
        return true
    }
}