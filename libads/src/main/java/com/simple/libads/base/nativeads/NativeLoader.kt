package com.simple.libads.base.nativeads

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.ShimmerFrameLayout
import com.simple.libads.ShimmerLayout
import com.simple.libads.admob.nativeads.view.BaseNativeAdView
import com.simple.libads.gone

abstract class NativeLoader( protected var adsId: String, @NativeType protected var nativeType: Int) {
    protected var TIME_OUT_NATIVE = 120
    protected var liveUpdate = MutableLiveData<AdsLoadingState>()
    protected var timeLoaded: Long = 0L
    protected var isLoading: Boolean = false
    var canRequest = true
        set(value) {
            field = value
            if(!canRequest) {
                liveUpdate.postValue(AdsLoadingState.STATE_ERROR)
            }
        }

    fun identify() = adsId + nativeType
    fun liveUpdate(): MutableLiveData<AdsLoadingState> = liveUpdate
    abstract fun bind(view: ViewGroup, context: Context)
    fun hide(view: ViewGroup) {
        view.gone()
        view.changeSize(0,0)
    }
    fun changeNativeType(@NativeType nativeType: Int) {
        this.nativeType = nativeType
    }
    abstract fun bindPlaceHolder(view: ViewGroup, context: Context)
    abstract fun loadAds(context: Context)
    abstract fun isReady(): Boolean
    open fun createCustomNativeView(context: Context): BaseNativeAdView? = null
    open fun createCustomHolder(context: Context): ShimmerFrameLayout? = null

    abstract fun isLoadFailed(): Boolean
    abstract fun reloadAds(context: Context)
    fun checkAdsEnableWidthSize(view: ViewGroup) {
        if(!isReady()) {
            view.changeSize(0,0)
        } else {
            view.changeSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    fun View.changeSize(
        width: Int?= null,
        height: Int ?= null
    ) {
        val lp = this.layoutParams
        width?.let {
            lp.width = width
        }
        height?.let {
            lp.height = height
        }

        if(width != null || height != null) {
            this.layoutParams = lp
        }
    }
}

enum class AdsLoadingState {
    STATE_LOADING,
    STATE_LOADED,
    STATE_ERROR
}