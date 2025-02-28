package com.simple.libads.manager

import android.content.Context
import android.view.ViewGroup
import com.simple.libads.base.nativeads.AdsLoadingState
import com.simple.libads.base.nativeads.NativeLoader
import com.simple.libads.base.nativeads.NativeType

class EmptyNativeLoader: NativeLoader("", NativeType.TYPE_MAX_CTA_BOTTOM) {
    override fun bind(view: ViewGroup, context: Context) {
        hide(view)
    }

    override fun bindPlaceHolder(view: ViewGroup, context: Context) {
        hide(view)
    }

    override fun loadAds(context: Context) {
        liveUpdate.postValue(AdsLoadingState.STATE_ERROR)
    }

    override fun isReady(): Boolean {
        return false
    }

    override fun isLoadFailed(): Boolean {
        return true
    }

    override fun reloadAds(context: Context) {
    }
}