package com.simple.libads.manager

import android.app.Activity
import android.content.Context
import com.simple.libads.AdNetworkType
import com.simple.libads.AdsApplication
import com.simple.libads.admob.openads.AdmobOpenApplicationLoader
import com.simple.libads.base.OpenAdLoader
import com.simple.libads.config.OpenConfig

class OpenApplicationManager {
    private var mListOpenLoader = HashMap<String, OpenWrapLoader>()
    private var mLoaderReady = HashMap<String, String>()


    fun init(configs: List<OpenConfig>) {
        configs.forEach {
            if(!mLoaderReady.containsKey(it.toString())) {
                mListOpenLoader[it.toString()] = createOpenAdLoader(it)
            } else {
                mListOpenLoader[it.toString()]?.loader?.setup(AdsApplication.appContext, it.adid)
            }
        }
    }

    private fun createOpenAdLoader(config: OpenConfig): OpenWrapLoader {
        return when(config.adnetwork) {
            AdNetworkType.ADMOB -> OpenWrapLoader(config, AdmobOpenApplicationLoader())

            else -> {
                OpenWrapLoader(config, EmptyOpenLoader())
            }
        }
    }

    fun setup(context: Context, config: OpenConfig) {
        mLoaderReady[config.toString()] = config.toString()
        var loader = mListOpenLoader[config.toString()]
        if(loader == null) {
            loader = createOpenAdLoader(config)
            mListOpenLoader[config.toString()] = loader
        }

        loader.loader.setup(context, config.adid)

    }

    fun isShowAds(): Boolean {
        for (key in mListOpenLoader.keys) {
            if(mListOpenLoader[key]?.loader?.isShowingAd == true) {
                return true
            }
        }
        return false
    }

    fun showAdIfAvailable(activity: Activity) {
        for (key in mListOpenLoader.keys) {
            mListOpenLoader[key]?.loader?.showAdIfAvailable(activity)
        }
    }

    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OpenAdLoader.OnShowAdCompleteListener) {
        val loader = ArrayList(mListOpenLoader.values).sortedBy { it.config.priority }
        showAds(activity, loader, 0, onShowAdCompleteListener)
    }

    private fun showAds(activity: Activity, list: List<OpenWrapLoader>, index: Int, onShowAdCompleteListener: OpenAdLoader.OnShowAdCompleteListener) {
        if(index >= list.size) {
            onShowAdCompleteListener.onShowAdComplete(false)
            return
        }
        val loader1 = list.getOrNull(index)?.loader ?: EmptyOpenLoader()
        loader1.showAdIfAvailable(activity, object : OpenAdLoader.OnShowAdCompleteListener {
            override fun onShowAdComplete(isShow: Boolean) {
                if(!isShow) {
                    if(index == list.size - 1) {
                        onShowAdCompleteListener.onShowAdComplete(false)
                    } else {
                        showAds(activity, list, index + 1, onShowAdCompleteListener)
                    }
                } else {
                    onShowAdCompleteListener.onShowAdComplete(true)
                }
            }
        })
    }
    fun loadAd(activity: Activity) {
        for (key in mListOpenLoader.keys) {
            mListOpenLoader[key]?.loader?.loadAd(activity)
        }
    }

    class OpenWrapLoader (
        var config: OpenConfig,
        var loader: OpenAdLoader
    )
}