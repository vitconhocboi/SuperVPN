package com.simple.libads.manager

import android.app.Activity
import androidx.privacysandbox.ads.adservices.adid.AdId
import com.simple.libads.AdNetworkType
import com.simple.libads.NetworkUtils
import com.simple.libads.admob.interads.AdmobInterLoader
import com.simple.libads.admob.rewardads.AdmobRewardLoader
import com.simple.libads.base.reward.RewardAdsLoader
import com.simple.libads.config.InterConfig

class RewardManager private constructor(){

    companion object {
        private var mRewardManager: RewardManager? = null

        fun getInstance(): RewardManager {
            if (mRewardManager == null) {
                mRewardManager = RewardManager()
            }
            return mRewardManager!!
        }
    }

    private var mRewardLoaders: ArrayList<RewardAdsLoader> = arrayListOf()

    fun addConfig(interConfigs: List<InterConfig>) {
        interConfigs.forEach { interConfig ->
            when (interConfig.adnetwork) {
                AdNetworkType.ADMOB -> {
                    if(mRewardLoaders.find { interConfig.adid == it.adId } == null) {
                        mRewardLoaders.add(AdmobRewardLoader(adId = interConfig.adid, placementId = interConfig.placement_id))
                    }
                }
            }
        }
    }

    fun createRewardAds(activity: Activity) {
        mRewardLoaders.forEach {
            it.loadRewardAds(activity)
        }
    }

    fun registerLoadCallBack(onLoadSuccess: ((() -> Unit)?), onLoadError: (() -> Unit)?) {
        val listAds = getRewardAds()
        listAds.forEach {
            it.registerLoadCallBack(onLoadSuccess, onLoadError)
        }
    }

    fun showRewardAds(
        activity: Activity,
        showCallBack: ((isShow: Boolean, isClaim: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {}
    ) {
        val ads = getRewardAds()
        val call0 = ads.getOrNull(0) ?: EmptyRewardManager()
        val call1 = ads.getOrNull(1) ?: EmptyRewardManager()

        call0.showRewardAds(activity, showCallBack = { isShow, isClaim ->
            if(!isShow) {
                call1.showRewardAds(activity, showCallBack = { isShow1, isClaim1 ->
                    showCallBack.invoke(isShow1, isClaim1)
                }, impressedCallBack = {
                    impressedCallBack.invoke()
                })
            } else {
                showCallBack.invoke(isShow, isClaim)
            }
        }, impressedCallBack = {
            impressedCallBack.invoke()
        })

    }

    private fun getRewardAds(): List<RewardAdsLoader> {
        val listAds = ArrayList<RewardAdsLoader>()
        mRewardLoaders.forEach {
            listAds.add(it)
        }
        return listAds
    }

    fun onResume(activity: Activity) {
        mRewardLoaders.forEach { it.onResume(activity) }
    }

    fun onPause(activity: Activity) {
        mRewardLoaders.forEach { it.onPause(activity) }
    }


    fun isRewardReady(activity: Activity): Boolean {
        mRewardLoaders.forEach { it.loadRewardAds(activity) }
        return mRewardLoaders.count { it.isReady() } > 0
    }
}