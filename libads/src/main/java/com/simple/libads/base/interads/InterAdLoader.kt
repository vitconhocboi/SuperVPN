package com.simple.libads.base.interads

import android.app.Activity
import android.app.Dialog
import com.simple.libads.AdsApplication
import com.simple.libads.DialogLoadingBuilder
import com.simple.libads.DialogTitle

abstract class InterAdLoader(var adId: String) {
    protected var mIsLoadFail: Boolean = false
    protected var mIsLoading: Boolean = false

    protected var mShowCallBack: ((isShow: Boolean) -> Unit) ?= null
    protected var mImpressedCallBack: (() -> Unit) ?= null

    protected var mOnLoader: ((isLoaded: Boolean) -> Unit) ?= null


    private var loadingDialog : Dialog?= null
    val canRequest : Boolean
        get() = AdsApplication.adsEnable
    protected val MAX_RETRY = 5.0
    open fun onResume(activity: Activity) {}
    open  fun onPause(activity: Activity) {}
    open fun onInitComplete(activity: Activity) {}
    abstract fun showInterAds(activity: Activity, showCallBack: ((isShow: Boolean) -> Unit), impressedCallBack: () -> Unit = {})
    abstract fun loadAndShowInterAds(activity: Activity, showCallBack: ((isShow: Boolean) -> Unit), impressedCallBack: () -> Unit = {})
    abstract fun loadInterAds(activity: Activity, onLoader: ((isLoaded: Boolean) -> Unit)?)
    abstract fun isReady(): Boolean
    abstract fun isLoadFail(): Boolean

    protected fun showDialogLoading(activity: Activity) {
        try {
            if(activity.isFinishing) return
            if(loadingDialog == null) {
                loadingDialog = AdsApplication.dialogLoadingBuilder.build(activity, DialogLoadingBuilder.Type.INTER)
            }
            (loadingDialog as? DialogTitle)?.setMessage(AdsApplication.dialogLoadingBuilder.getInterMessage())
            loadingDialog?.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    protected fun dismissDialog(activity: Activity) {
        try {
            if(activity.isFinishing) return
            loadingDialog?.dismiss()
            loadingDialog = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}