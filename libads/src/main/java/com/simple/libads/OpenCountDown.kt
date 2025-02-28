package com.simple.libads

import android.app.Activity
import android.app.Dialog
import androidx.lifecycle.Lifecycle
import com.simple.libads.admob.interads.AdmobInterLoader
import com.simple.libads.base.interads.InterAdLoader
import com.simple.libads.admob.openads.AdmobOpenAdLoader
import com.simple.libads.config.OpenConfig

open class OpenCountDown(var configs: List<OpenConfig>, var onNextScreen: () -> Unit = {}) {
    private val COUNTER_TIME = 5_000L
    private val COUNTER_ALL_AD = 4_000L
    private var mStart = System.currentTimeMillis()
    private var mListOpenLoader = ArrayList<InterAdLoader>()
    var isNetworkAvailable = true
    var byPass = false
    init {
        configs.forEach {
            when(it.adnetwork) {
                AdNetworkType.ADMOB -> {
                    if(it.ad_type == AdType.OPEN) {
                        mListOpenLoader.add(AdmobOpenAdLoader(it.adid))
                    } else {
                        mListOpenLoader.add(AdmobInterLoader(it.adid))
                    }

                }
            }
        }
    }


    private var mLoadingDialog: Dialog?= null

    fun startCountDown(activity: Activity, lifecycle: Lifecycle) {
        if (byPass) {
            onNextScreen.invoke()
            return
        }
        mStart = System.currentTimeMillis()
        mListOpenLoader.forEach { it.loadInterAds(activity, {})}
        postDelay(2000) {
            if(mListOpenLoader.isEmpty()) {
                onNextScreen.invoke()
            } else {
                if(lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    postCountDown(activity, 0)
                }
            }
        }
    }

    private fun postCountDown(activity: Activity, seconds: Long) {
        if (seconds > COUNTER_TIME || !AdsApplication.adsEnable || !isNetworkAvailable) {
            onNextScreen()
        } else {
            val first = mListOpenLoader.firstOrNull()
            val firstReady = mListOpenLoader.firstOrNull { it.isReady() }
            if(mListOpenLoader.count { !it.isLoadFail() } <= 0) {
                onNextScreen()
            } else if(first?.isReady() == true) {
                showDialogLoading(activity)
                postDelay(1000) {
                    dismissDialog(activity)
                    first.showInterAds(activity = activity, {
                        onNextScreen()
                    })
                }
            } else if(seconds >= COUNTER_ALL_AD && firstReady != null) {
                firstReady.showInterAds(activity = activity, {
                    onNextScreen()
                })
            } else {
                postDelay(1000) {
                    val remain = System.currentTimeMillis() - mStart
                    postCountDown(activity, remain)
                }
            }
        }
    }

    protected fun showDialogLoading(activity: Activity) {
        try {
            if(activity.isFinishing) return
            if(mLoadingDialog == null) {
                mLoadingDialog = AdsApplication.dialogLoadingBuilder.build(activity, DialogLoadingBuilder.Type.INTER)
            }
            (mLoadingDialog as? DialogTitle)?.setMessage(AdsApplication.dialogLoadingBuilder.getInterMessage())
            mLoadingDialog?.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    protected fun dismissDialog(activity: Activity) {
        try {
            if (activity.isFinishing) return
            mLoadingDialog?.dismiss()
            mLoadingDialog = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}