package com.simple.libads.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.simple.libads.AdsApplication
import com.simple.libads.DialogLoadingBuilder
import com.simple.libads.DialogTitle

abstract class OpenAdLoader {
    var isShowingAd = false
    protected var idAds: String = ""
    protected var isSetup = true
    protected var isLoadingAd = false
    private var loadingDialog: Dialog? = null
    protected val adEnable: Boolean
        get() {
            return AdsApplication.adsEnable
        }


    abstract fun showAdIfAvailable(activity: Activity)
    abstract fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener,
    )

    open fun setup(context: Context, idAds: String) {
        isSetup = true
        this.idAds = idAds
        if (adEnable) {
            loadAd(context)
        }
    }

    abstract fun loadAd(context: Context)

    protected fun showDialogLoading(activity: Activity) {
        try {
            if(activity.isFinishing) return
            if (loadingDialog == null) {
                loadingDialog = AdsApplication.dialogLoadingBuilder.build(
                    activity,
                    DialogLoadingBuilder.Type.OPEN
                )
            }
            (loadingDialog as? DialogTitle)?.setMessage(AdsApplication.dialogLoadingBuilder.getOpenMessage())
            loadingDialog?.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    protected fun dismissDialog(activity: Activity) {
        try {
            if (activity.isFinishing) return
            loadingDialog?.dismiss()
            loadingDialog = null

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete(isShow: Boolean)
    }
}