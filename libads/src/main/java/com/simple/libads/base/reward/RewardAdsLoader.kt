package com.simple.libads.base.reward

import android.app.Activity

abstract class RewardAdsLoader(var adId: String, var placementId: String) {
    protected var mShowCallBack: ((isShow: Boolean, isClaim: Boolean) -> Unit)? = null
    protected var mImpressedCallBack: (() -> Unit)? = null
    protected var mLoadSuccess: (() -> Unit)? = null
    protected var mLoadError: (() -> Unit)? = null
    open fun onResume(activity: Activity) {}
    open fun onPause(activity: Activity) {}
    open fun onInitComplete(activity: Activity) {}
    abstract fun showRewardAds(activity: Activity, showCallBack: ((isShow: Boolean, isClaim: Boolean) -> Unit), impressedCallBack: () -> Unit = {})
    abstract fun loadRewardAds(activity: Activity)

    fun registerLoadCallBack(onLoadSuccess: (() -> Unit)?, onLoadError: (() -> Unit)?) {
        mLoadSuccess = onLoadSuccess
        mLoadError = onLoadError
        if(isReady()) {
            onLoadSuccess?.invoke()
        }
    }
    abstract fun isReady(): Boolean
}