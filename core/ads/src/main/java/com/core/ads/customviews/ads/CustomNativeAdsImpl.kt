package com.core.ads.customviews.ads

import android.content.Context
import com.core.ads.R
import com.core.config.domain.data.NativeAdPlace
import com.core.config.domain.data.NativeTemplateSize

class CustomNativeAdsImpl : CustomNativeAds {
    override fun createNativeAds(context: Context, nativeAdPlace: NativeAdPlace): BaseNativeTemplateView {
        return NativeSmallTemplateView(context)
    }

    override fun createShimmerLayoutPlaceHolder(nativeTemplateSize: NativeTemplateSize): Int {
        return R.layout.gnt_small_template_view_shimmer
    }
}