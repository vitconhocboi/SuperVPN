package com.simple.libads.manager

import com.simple.libads.AdNetworkType
import com.simple.libads.admob.nativeads.loader.AdmobNativeLoader
import com.simple.libads.base.nativeads.NativeLoader
import com.simple.libads.base.nativeads.NativeType
import com.simple.libads.config.NativeBannerConfig

object NativeManager {
    fun createLoader(@AdNetworkType adNetwork: String, adid: String, @NativeType uiType: Int): NativeLoader? {
        return when (adNetwork) {
            AdNetworkType.ADMOB -> {
                AdmobNativeLoader(adid, uiType)
            }

            else -> null
        }
    }

    fun createAdmobLoader(adid: String, @NativeType uiType: Int): NativeLoader {
        return AdmobNativeLoader(adid, uiType)
    }

    fun createNativeWaterfall(configs: List<NativeBannerConfig>): NativeWaterFall {
        return NativeWaterFall(configs)
    }
}