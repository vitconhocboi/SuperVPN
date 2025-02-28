package com.simple.libads.manager

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.simple.libads.AdNetworkType
import com.simple.libads.FrameAds
import com.simple.libads.admob.nativeads.loader.AdmobNativeLoader
import com.simple.libads.base.nativeads.AdsLoadingState
import com.simple.libads.base.nativeads.NativeLoader
import com.simple.libads.config.NativeBannerConfig

class NativeWaterFall(private var configs: List<NativeBannerConfig>) {
    private val mEmptyNativeLoader: NativeLoader by lazy {
        EmptyNativeLoader()
    }
    private var mAdNativeLoader = arrayListOf<NativeWrapLoader>()

    init {
        configs.forEach {
            when (it.adnetwork) {
                AdNetworkType.ADMOB -> {
                    mAdNativeLoader.add(NativeWrapLoader(it, AdmobNativeLoader(adsId = it.adid, nativeType = it.style)))
                }
            }
        }
    }

    fun loadNative(owner: LifecycleOwner, frameAds: FrameAds, context: Context, loadingListener: NativeLoadingListener) {
        loadNative(listLoader = mAdNativeLoader, owner = owner, frameAds = frameAds, context = context, loadingListener = loadingListener, index = 0)
    }
    private fun loadNative(listLoader: List<NativeWrapLoader>, owner: LifecycleOwner, frameAds: FrameAds, context: Context, loadingListener: NativeLoadingListener, index: Int) {
        if(index >= listLoader.size) {
            mEmptyNativeLoader.loadAds(context)
            mEmptyNativeLoader.hide(view = frameAds)
            loadingListener.onNativeError()
            return
        }

        val native1 = mAdNativeLoader.getOrNull(index)?.nativeLoader ?: EmptyNativeLoader()
        native1.apply {
            liveUpdate().observe(owner) { stateLoading1 ->
                when (stateLoading1) {
                    AdsLoadingState.STATE_ERROR -> {
                        loadNative(listLoader, owner, frameAds, context, loadingListener, index + 1)
                    }

                    AdsLoadingState.STATE_LOADED -> {
                        bind(frameAds, context)
                        loadingListener.onNativeLoaded()
                    }

                    else -> {
                        bindPlaceHolder(frameAds, context)
                        loadingListener.onNativeLoading()
                    }
                }
            }
            loadAds(context)

        }
    }

    fun isNativeLoadFail(): Boolean {
        return mAdNativeLoader.count { !it.nativeLoader.isLoadFailed() } <= 0
    }

    fun isNativeReady(): Boolean {
        return mAdNativeLoader.count { it.nativeLoader.isReady() } > 0
    }

    private fun nativeLoaderReady(): NativeWrapLoader? {
        return mAdNativeLoader.firstOrNull { it.nativeLoader.isReady() }
    }

    fun hideOnRecycleAdapter(frameAds: FrameAds) {
        mEmptyNativeLoader.hide(frameAds)
    }

    fun bindPlaceHolderOnRecycleAdapter(frameAds: FrameAds, context: Context)  {
        mAdNativeLoader.firstOrNull()?.apply {
            nativeLoader.changeNativeType(config.otherStyle ?: config.style)
            nativeLoader.bindPlaceHolder(frameAds, context)
        }
    }

    fun bindOnRecycleAdapter(frameAds: FrameAds, context: Context)  {
        nativeLoaderReady()?.apply {
            nativeLoader.changeNativeType(config.otherStyle ?: config.style)
            nativeLoader.bind(frameAds, context)
        }
    }


    class NativeWrapLoader(
        var config: NativeBannerConfig,
        var nativeLoader: NativeLoader
    )

    abstract class  NativeLoadingListener {
        open fun onNativeLoaded(){}
        open fun onNativeLoading(){}
        open fun onNativeError(){}
    }

}