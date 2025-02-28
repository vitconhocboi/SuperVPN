package com.simple.libads.base.bannerads

import android.content.Context
import android.util.Size
import com.simple.libads.FrameAds

abstract class BannerLoader(protected var bannerId: String) {
    var canRequest: Boolean = true

    abstract fun loadAds(
        @BannerType bannerType: Int? = null,
        @BannerAnchored bannerAnchored: String? = null,
        context: Context,
        parent: FrameAds,
        onLoaded: ((isSuccess: Boolean) -> Unit)?= null,
        onSizeChange: ((size: Size) -> Unit) ?= null,
        isGoneWhenFail: Boolean = true
    )

    abstract fun destroy()
}