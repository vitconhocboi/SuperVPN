package com.simple.libads.admob.interads


import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.simple.libads.AdsApplication.Companion.isShowInterAds
import com.simple.libads.SessionHelper
import com.simple.libads.base.interads.InterAdLoader
import com.simple.libads.postDelay
import kotlinx.coroutines.runBlocking

class AdmobInterLoader(adId: String): InterAdLoader(adId) {
    private var mInterAd: InterstitialAd? = null


    override fun loadAndShowInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: (() -> Unit)
    ) {
        if(!canRequest || !SessionHelper.isCanShowInterstitial()) {
            showCallBack.invoke(false)
            return
        }

        if (mInterAd != null) {
            showInterAds(activity, showCallBack, impressedCallBack)
        } else if(!mIsLoading){
            showDialogLoading(activity)
            mIsLoading = true
            val adRequest = AdRequest.Builder().build()
            val interAdId = adId
            InterstitialAd.load(activity, interAdId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterAd = null
                    mIsLoading = false
                    mIsLoadFail = true
                    showCallBack.invoke(false)
                    dismissDialog(activity)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterAd = interstitialAd
                    mIsLoading = false
                    showInterAds(activity, showCallBack, impressedCallBack)
                }
            })
        }
    }

    override fun loadInterAds(activity: Activity, onLoader: ((isLoaded: Boolean) -> Unit)?) {
        if(!canRequest || !SessionHelper.isCanShowInterstitial()) return

        if (mInterAd != null || mIsLoading) {
            return
        }

        mIsLoading = true
        val adRequest = AdRequest.Builder().build()

        val interAdId = adId
        InterstitialAd.load(activity, interAdId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterAd = null
                mIsLoading = false
                mIsLoadFail = true
                onLoader?.invoke(false)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterAd = interstitialAd
                mIsLoading = false
                onLoader?.invoke(true)
            }
        })
    }

    override fun showInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: () -> Unit
    ) {
        if(!canRequest || !SessionHelper.isCanShowInterstitial()) {
            showCallBack.invoke(false)
            return
        }
        if (mInterAd != null) {
            showDialogLoading(activity)
            postDelay(800) {
                mInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        mInterAd = null
                        showCallBack(true)
                        loadInterAds(activity, {})
                        runBlocking {
                            isShowInterAds.emit(false)
                        }
                        SessionHelper.setTimeShowInterstitial()
                        dismissDialog(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        showCallBack(false)
                        runBlocking {
                            isShowInterAds.emit(false)
                        }
                        dismissDialog(activity)
                    }

                    override fun onAdShowedFullScreenContent() {
                        runBlocking {
                            isShowInterAds.emit(true)
                        }
                        dismissDialog(activity)
                        impressedCallBack.invoke()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        mInterAd = null
                        runBlocking {
                            isShowInterAds.emit(true)
                        }
                        dismissDialog(activity)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                    }

                }
                mInterAd?.show(activity)
            }
        } else {
            loadInterAds(activity, {})
            showCallBack(false)
        }
    }

    override fun isReady(): Boolean {
        return mInterAd != null
    }

    override fun isLoadFail(): Boolean {
        return mIsLoadFail
    }
}