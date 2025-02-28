package com.simple.libads

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simple.libads.config.InterConfig
import com.simple.libads.manager.InterManager
import com.simple.libads.manager.RewardManager

abstract class AdsActivity : AppCompatActivity() {

    protected var mRewardManager: RewardManager? = null
    abstract fun interConfigs(): List<InterConfig>?
    abstract fun interConfigsWithoutVideo(): List<InterConfig>?
    abstract fun rewardConfigs(): List<InterConfig>?

    protected var mInterManager: InterManager? = null
    protected var mInterManagerWithoutVideo: InterManager? = null

    abstract fun isBuyApp(): Boolean

    private var onLoadRewardSuccess: (() -> Unit)? = null
    private var onLoadRewardError: (() -> Unit)? = null

    fun loadAds() {
        loadInterAd(interConfigs())
        loadInterAdWithoutVideo(interConfigsWithoutVideo())
        loadRewardAd(rewardConfigs())
    }

    private fun loadInterAdWithoutVideo(list: List<InterConfig>?) {
        if (isBuyApp()) return
        list?.let {
            mInterManagerWithoutVideo = InterManager.getInstanceWithoutVideo()
            mInterManagerWithoutVideo?.addConfig(list)
            mInterManagerWithoutVideo?.createInterAds(this)
        }
    }

    private fun loadInterAd(list: List<InterConfig>?) {
        if (isBuyApp()) return
        list?.let {
            mInterManager = InterManager.getInstance()
            mInterManager?.addConfig(list)
            mInterManager?.createInterAds(this)
        }
    }

    private fun loadRewardAd(list: List<InterConfig>?) {
//        if (isBuyApp()) return /*TODO*/
        list?.let {
            mRewardManager = RewardManager.getInstance()
            mRewardManager?.addConfig(list)
            mRewardManager?.createRewardAds(this)
        }
    }

    fun reloadRewardAds() {
        mRewardManager?.createRewardAds(this)
    }

    fun registerRewardLoad(onLoadSuccess: (() -> Unit)?, onLoadError: (() -> Unit)?) {
        onLoadRewardSuccess = onLoadSuccess
        onLoadRewardError = onLoadError
        mRewardManager?.registerLoadCallBack(onLoadSuccess = {
            onLoadRewardSuccess?.invoke()
        }, onLoadError = {
            onLoadRewardError?.invoke()
        })
    }

    fun unregisterRewardLoadSuccess() {
        onLoadRewardSuccess = null
        onLoadRewardError = null
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterRewardLoadSuccess()
    }


    fun showInterAds(
        placementId: String,
        showCallBack: ((isShow: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {}
    ) {
        if (isBuyApp() || interConfigs()?.find { it.placement_id == placementId } == null) {
            showCallBack.invoke(false)
            return
        }
        if (mInterManager != null) {
            mInterManager?.showInterstitial(this, showCallBack, impressedCallBack)
        } else {
            showCallBack.invoke(false)
        }
    }

    fun showInterAdsWithoutVideo(
        placementId: String,
        showCallBack: ((isShow: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {},
    ) {
        if (isBuyApp() || interConfigsWithoutVideo()?.find { it.placement_id == placementId } == null) {
            showCallBack.invoke(false)
            return
        }
        if (mInterManagerWithoutVideo != null) {
            mInterManagerWithoutVideo?.showInterstitial(this, showCallBack, impressedCallBack)
        } else {
            showCallBack.invoke(false)
        }
    }

    fun isRewardReady() = mRewardManager?.isRewardReady(this) ?: false


    fun showRewardAds(
        placementId: String,
        showCallBack: ((isShow: Boolean, isClaim: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {}
    ) {
        if (/*isBuyApp() || */mRewardManager == null && rewardConfigs()?.find { it.placement_id == placementId } == null) {
            showCallBack.invoke(false, false)
            return
        }

        if (isRewardReady() && NetworkUtils.isInternetAvailable(this)) {
            mRewardManager?.showRewardAds(
                activity = this,
                showCallBack = showCallBack,
                impressedCallBack = impressedCallBack
            )
        } else {
            if (NetworkUtils.isInternetAvailable(this)) {
                GDPRCheck {
                    showDialogLoadingAds()
                    registerRewardLoad(onLoadSuccess = {
                        unregisterRewardLoadSuccess()
                        mRewardManager?.showRewardAds(
                            activity = this,
                            showCallBack = showCallBack,
                            impressedCallBack = impressedCallBack
                        )
                        dismissDialogLoadingAds()
                    }, onLoadError = {
                        dismissDialogLoadingAds()
                        unregisterRewardLoadSuccess()
                    })
                    reloadRewardAds()
                }
            } else {
                showDialogNoInternet(onRetry = {showRewardAds(placementId,showCallBack,impressedCallBack)})
            }
        }


    }

    abstract fun dismissDialogLoadingAds()

    abstract fun showDialogLoadingAds()

    abstract fun GDPRCheck(onGrant: () -> Unit)

    abstract fun showDialogNoInternet(onRetry: () -> Unit)

    abstract fun showDialogNoAds()

    abstract fun toastNetworkError()

    override fun onResume() {
        super.onResume()
        if (!isBuyApp()) {
            mInterManager?.onResume(this)
            mRewardManager?.onResume(this)
            registerRewardLoad(onLoadRewardSuccess, onLoadRewardError)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        if (!isBuyApp()) {
            mRewardManager?.onPause(this)
        }
    }
}