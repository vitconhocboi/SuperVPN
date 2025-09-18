package com.core.ads.customviews.ads

class CustomNativeProvider {
    companion object {
        private val instance = CustomNativeProvider()
        fun getInstance(): CustomNativeProvider {
            return instance
        }
    }

    private var customNativeAds: CustomNativeAds = CustomNativeAdsImpl()

    fun setCustomNativeAds(customNativeAds: CustomNativeAds) {
        this.customNativeAds = customNativeAds
    }

    fun getCustomNativeAds(): CustomNativeAds {
        return customNativeAds
    }
}