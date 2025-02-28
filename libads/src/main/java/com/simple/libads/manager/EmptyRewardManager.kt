package com.simple.libads.manager

import android.app.Activity
import com.simple.libads.base.reward.RewardAdsLoader

class EmptyRewardManager: RewardAdsLoader("", "") {

    override fun loadRewardAds(activity: Activity) {

    }

    override fun showRewardAds(
        activity: Activity,
        showCallBack: (isShow: Boolean, isClaim: Boolean) -> Unit,
        impressedCallBack: () -> Unit,
    ) {
        showCallBack.invoke(false, false)

    }

    override fun isReady(): Boolean {
        return false
    }

    override fun onResume(activity: Activity) {
        loadRewardAds(activity)
    }
}