package com.simple.libads.manager

import android.app.Activity
import com.simple.libads.AdNetworkType
import com.simple.libads.admob.interads.AdmobInterLoader
import com.simple.libads.base.EmptyInterAdManager
import com.simple.libads.base.interads.InterAdLoader
import com.simple.libads.config.InterConfig

class InterManager private constructor(){


    private var mInterLoaders = ArrayList<InterAdLoader>()

    companion object {
        private var InterManager: InterManager? = null

        fun getInstance(): InterManager {
            if (InterManager == null) {
                InterManager = InterManager()
            }
            return InterManager!!
        }

        private var mInterManagerWithoutVideo: InterManager? = null

        fun getInstanceWithoutVideo(): InterManager {
            if (mInterManagerWithoutVideo == null) {
                mInterManagerWithoutVideo = InterManager()
            }
            return mInterManagerWithoutVideo!!
        }
    }

    fun addConfig(interConfigs: List<InterConfig>) {
        interConfigs.forEach { interConfig ->
            when (interConfig.adnetwork) {
                AdNetworkType.ADMOB -> {
                    if(mInterLoaders.find { interConfig.adid == it.adId } == null) {
                        mInterLoaders.add(AdmobInterLoader(adId = interConfig.adid))
                    }
                }
            }
        }
    }

    fun createInterAds(activity: Activity) {
        mInterLoaders.forEach {
            it.loadInterAds(activity, {})
        }
    }

    private fun getPlayAds(): List<InterAdLoader> {
        val listAds = ArrayList<InterAdLoader>()
        mInterLoaders.forEach {
            listAds.add(it)
        }
        return listAds
    }

    fun showInterstitial(
        activity: Activity,
        showCallBack: ((isShow: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {}
    ) {
        val listAds = getPlayAds()
        val call0 = listAds.getOrNull(0) ?: EmptyInterAdManager("")
        val call1 = listAds.getOrNull(1) ?: EmptyInterAdManager("")
        val call2 = listAds.getOrNull(2) ?: EmptyInterAdManager("")
        call0.showInterAds(activity = activity, impressedCallBack = {
            impressedCallBack.invoke()
        }, showCallBack = { isShow ->
            if (isShow) {
                showCallBack.invoke(isShow)
            } else {
                call1.showInterAds(activity = activity, impressedCallBack = {
                    impressedCallBack.invoke()
                }, showCallBack = { isShow1 ->
                    if (isShow1) {
                        showCallBack.invoke(isShow1)
                    } else {
                        call2.showInterAds(activity = activity, impressedCallBack = {
                            impressedCallBack.invoke()
                        }, showCallBack = { isShow2 ->
                            showCallBack.invoke(isShow2)
                        })
                    }
                })
            }
        })
    }

    fun onResume(activity: Activity) {
        mInterLoaders.forEach { it.onResume(activity) }
    }

}