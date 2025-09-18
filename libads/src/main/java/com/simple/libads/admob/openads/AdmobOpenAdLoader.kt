package com.simple.libads.admob.openads


import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.simple.libads.AdsApplication
import com.simple.libads.base.interads.InterAdLoader

class AdmobOpenAdLoader(adId: String) :
    InterAdLoader(adId) {
    private var mAppOpenAd: AppOpenAd? = null

    override fun loadAndShowInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: () -> Unit
    ) {
        showCallBack.invoke(false)
    }

    override fun showInterAds(
        activity: Activity,
        showCallBack: (isShow: Boolean) -> Unit,
        impressedCallBack: () -> Unit
    ) {
        if (mAppOpenAd != null && !AdsApplication.isShowInterAds.value && canRequest) {
            mAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    mAppOpenAd = null
                    showCallBack.invoke(true)
                        AdsApplication.isShowInterAds.value = false
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    showCallBack.invoke(false)
                    AdsApplication.isShowInterAds.value = false
                }

                override fun onAdShowedFullScreenContent() {
                    AdsApplication.isShowInterAds.value = true
                    impressedCallBack.invoke()
                }

            }
            mAppOpenAd?.show(activity)

        } else {
            showCallBack.invoke(false)
        }

    }

    override fun loadInterAds(activity: Activity, onLoader: ((isLoaded: Boolean) -> Unit)?) {
        if (mAppOpenAd != null || mIsLoading || !canRequest) return
        val adRequest = AdRequest.Builder().build()
        mIsLoading = true

        AppOpenAd.load(
            activity,
            adId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mAppOpenAd = null
                    mIsLoading = false
                    mIsLoadFail = true
                    onLoader?.invoke(false)
                }

                override fun onAdLoaded(p0: AppOpenAd) {
                    mAppOpenAd = p0
                    mIsLoadFail = false
                    mIsLoading = false
                    onLoader?.invoke(true)
                }

            })
    }

    override fun isLoadFail(): Boolean {
        return mIsLoadFail
    }

    override fun isReady(): Boolean {
        return mAppOpenAd != null
    }
}