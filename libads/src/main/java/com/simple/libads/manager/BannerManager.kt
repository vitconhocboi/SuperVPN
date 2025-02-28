package com.simple.libads.manager

import com.simple.libads.AdNetworkType
import com.simple.libads.admob.bannerads.AdmobBannerLoader
import com.simple.libads.base.bannerads.BannerLoader

object BannerManager {
    fun createLoader(@AdNetworkType adNetwork: String, bannerId: String): BannerLoader? {
        return when (adNetwork) {
            AdNetworkType.ADMOB -> {
                AdmobBannerLoader(bannerId)
            }

            else -> null
        }
    }
}