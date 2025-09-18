package com.core.ads.customviews.ads

import android.content.Context
import androidx.annotation.LayoutRes
import com.core.config.domain.data.NativeAdPlace
import com.core.config.domain.data.NativeTemplateSize

interface CustomNativeAds {

    fun createNativeAds(context: Context, nativeAdPlace: NativeAdPlace): BaseNativeTemplateView

    @LayoutRes
    fun createShimmerLayoutPlaceHolder(nativeTemplateSize: NativeTemplateSize): Int
}