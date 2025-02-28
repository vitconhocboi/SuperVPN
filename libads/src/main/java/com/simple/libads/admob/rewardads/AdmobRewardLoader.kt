package com.simple.libads.admob.rewardads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.simple.libads.AdsApplication
import com.simple.libads.base.reward.RewardAdsLoader
import com.simple.libads.postDelay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class AdmobRewardLoader(adId: String, placementId: String) : RewardAdsLoader(adId, placementId) {
    private var rewardedAd: RewardedAd? = null
    private var retryAttemptReward = 0.0
    private val MAX_RETRY = 5
    private var rewardAdSuccess = false
    private var isLoading = false

 /*   companion object {
        private var mInstance: AdmobRewardLoader? = null

        fun getInstance(id: String): AdmobRewardLoader {
            return mInstance ?: synchronized(this) {
                mInstance ?: AdmobRewardLoader(id).also {
                    mInstance = it
                }
            }
        }
    }*/

    override fun loadRewardAds(activity: Activity) {
        if (rewardedAd == null && !isLoading) {
            isLoading = true
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity,
                adId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        retryAttemptReward++
                        if (retryAttemptReward < MAX_RETRY) {
                            val delayMillis =
                                TimeUnit.SECONDS.toMillis(
                                    2.0.pow(6.0.coerceAtMost(retryAttemptReward)).toLong()
                                )
                            postDelay(delayMillis) { loadRewardAds(activity) }
                        }
                        mLoadError?.invoke()
                        rewardedAd = null
                        isLoading = false
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        retryAttemptReward = 0.0
                        rewardedAd = ad
                        isLoading = false
                        mLoadSuccess?.invoke()
                    }
                })
        }

    }

    override fun showRewardAds(
        activity: Activity,
        showCallBack: (isShow: Boolean, isClaim: Boolean) -> Unit,
        impressedCallBack: () -> Unit,
    ) {
        mShowCallBack = showCallBack
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    mShowCallBack?.invoke(true, rewardAdSuccess)
                    AdsApplication.isShowInterAds.value = false
                    rewardedAd = null
                    loadRewardAds(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    AdsApplication.isShowInterAds.value = false
                    rewardedAd = null
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    mImpressedCallBack?.invoke()
                    AdsApplication.isShowInterAds.value = true
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    runBlocking {
                        AdsApplication.isShowInterAds.value = true
                    }
                }
            }
            rewardedAd?.let { ad ->
                ad.show(activity) { rewardItem ->
                    rewardAdSuccess = true
                }
            }
        } else {
            loadRewardAds(activity)
            mShowCallBack?.invoke(false, false)
        }
    }

    override fun isReady(): Boolean {
        return rewardedAd != null
    }

    override fun onResume(activity: Activity) {
        loadRewardAds(activity)
    }
}