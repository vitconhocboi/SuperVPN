package com.highsecure.vpn.proxy.master.remoteconfig


import com.common.baseui.SharedPrefs
import com.google.gson.Gson
import com.highsecure.vpn.proxy.master.BuildConfig
import com.simple.libads.AdNetworkType
import com.simple.libads.base.bannerads.BannerType
import com.simple.libads.base.nativeads.NativeType
import timber.log.Timber


class FirebaseConfigManager {

    val adWithoutVideoPlacement
        get() = mAdConfig?.interWithoutVideo ?: AdUnitInterAdmobItem(
            adId = BuildConfig.ads_full_without_video_new,
            placementIds = arrayListOf(
                AdPlacementId.INTER_ART_HOME,
                AdPlacementId.INTER_ART_TRENDING,
                AdPlacementId.INTER_ART_INCOMPLETE,
                AdPlacementId.INTER_ART_COMPLETED,
                AdPlacementId.INTER_ART_CREATOR_LIST,
                AdPlacementId.INTER_ART_CREATOR_CAMERA,
                AdPlacementId.INTER_ART_CREATOR_GALLERY,
                AdPlacementId.INTER_ART_PAINTING_60_PERCENT_COMPLETED
            )
        )

    val adWithVideo
        get() = mAdConfig?.interWithVideo ?: AdUnitInterAdmobItem(
//            adId = PixelApplication.instance.getString(R.string.ads_full_with_video),
            adId = BuildConfig.ads_full_with_video_new,
            placementIds = arrayListOf(
                AdPlacementId.INTER_ART_PAINTED
            )
        )

    val adNativeAds
        get() = mAdConfig?.nativeAds ?: arrayListOf(
            AdUnitNativeAdmobItem(
                //adId = PixelApplication.instance.getString(R.string.ads_native_id),
                adId = BuildConfig.ads_native_id_new,
                placementId = AdPlacementId.NATIVE_LANGUAGE,
                type = NativeType.TYPE_MEDIUM_CTA_BOTTOM
            ),AdUnitNativeAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.ads_native_id),
                adId = BuildConfig.ads_native_id_new,
                placementId = AdPlacementId.NATIVE_REWARDED,
                type = NativeType.TYPE_CUSTOM
            ),AdUnitNativeAdmobItem(
                //adId = PixelApplication.instance.getString(R.string.ads_native_id),
                adId = BuildConfig.ads_native_id_new,
                placementId = AdPlacementId.NATIVE_PAINTED,
                type = NativeType.TYPE_CUSTOM
            )
        )

    val adBanner
        get() = mAdConfig?.bannerAds ?: arrayListOf(
            AdUnitBannerAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.ads_banner_id),
                adId = BuildConfig.ads_banner_id_new,
                placementId = AdPlacementId.BANNER_HOME,
                adNetwork = AdNetworkType.ADMOB,
                adType = BannerType.TYPE_INLINE_ADAPTIVE
            ), AdUnitBannerAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.ads_banner_id),
                adId = BuildConfig.ads_banner_id_new,
                placementId = AdPlacementId.BANNER_PAINTING,
                adNetwork = AdNetworkType.ADMOB,
                adType = BannerType.TYPE_INLINE_ADAPTIVE
            ), AdUnitBannerAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.ads_banner_id),
                adId = BuildConfig.ads_banner_id_new,
                placementId = AdPlacementId.BANNER_CAMERA,
                adNetwork = AdNetworkType.ADMOB,
                adType = BannerType.TYPE_INLINE_ADAPTIVE
            ), AdUnitBannerAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.ads_banner_id),
                adId = BuildConfig.ads_banner_id_new,
                placementId = AdPlacementId.BANNER_PAINTED,
                adNetwork = AdNetworkType.ADMOB,
                adType = BannerType.TYPE_INLINE_ADAPTIVE
            )
        )

    val adRewardCommon
        get() = mAdConfig?.rewardAds ?: AdUnitRewardAdmobItem(
//            adId = PixelApplication.instance.getString(R.string.ads_reward_id),
            adId = BuildConfig.ads_reward_id_new,
            placementIds = arrayListOf(
                AdPlacementId.REWARD_FUNCTION_EDIT,
                AdPlacementId.REWARD_ART_HOME,
                AdPlacementId.REWARD_ART_DETAIL_CATEGORY,
            )
        )
    val adOpenAds
        get() = mAdConfig?.openAds ?: arrayListOf(
            AdUnitAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.app_ads_open_resume_id),
                adId = BuildConfig.app_ads_open_resume_id_new,
                placementId = AdPlacementId.OPEN_SPLASH
            ), AdUnitAdmobItem(
//                adId = PixelApplication.instance.getString(R.string.app_ads_open_resume_id),
                adId = BuildConfig.app_ads_open_resume_id_new,
                placementId = AdPlacementId.OPEN_RESUME
            )
        )

    private var mAdConfig: AdConfig? = null
//    private var mPixelConfig: PixelConfig? = null
//    val pixelConfig: PixelConfig?
//        get() {
//            return mPixelConfig
//        }

//    private var mConfigLoader: ConfigLoader = if (BuildConfig.FIREBASE_DEBUG) {
//        LocalConfigLoader()
//    } else {
//        FirebaseConfigLoader()
//    }

    companion object {
        const val KEY_AD_CONFIG = "ad_config"
        const val KEY_APP_CONFIG = "app_config"

        private val instance by lazy {
            FirebaseConfigManager()
        }

        fun get(): FirebaseConfigManager {
            return instance
        }
    }

//    init {
//        val config = SharedPrefs.instance[KEY_AD_CONFIG, String::class.java, ""]
//        try {
//            if (config.isNotEmpty()) {
//                setConfig(config, false)
//            }
//        } catch (ex: Exception) {
//
//        }
//    }

//    fun setConfig(json: String, fromServer: Boolean) {
//        try {
//            if (json.isEmpty()) return
//            if (fromServer) {
//                SharedPrefs.instance.put(KEY_AD_CONFIG, json)
//            }
//            Timber.d("json config $json")
//            // Open the asset file
//            // Parse the JSON string into a JsonObject
//            mAdConfig = Gson().fromJson(json, AdConfig::class.java)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    fun setAppConfig(json: String, fromServer: Boolean) {
//        try {
//            if (json.isEmpty()) return
//            if (fromServer) {
//                SharedPrefs.instance.put(KEY_APP_CONFIG, json)
//            }
//            Timber.d("json config $json")
//            // Open the asset file
//            // Parse the JSON string into a JsonObject
//            mPixelConfig = Gson().fromJson(json, PixelConfig::class.java)
//            mPixelConfig?.let { config ->
//                AppConfig.BOMB_DEFAULT = config.defaultBomb ?: 10
//                AppConfig.MAGNIFY_DEFAULT = config.defaultMagnify ?: 10
//                AppConfig.FLOOD_FILL_DEFAULT = config.defaultFill ?: 10
//                AppConfig.FILL_NUMBER = config.defaultFillNumber ?: 10
//                AppConfig.MINIMUM = config.defaultWhenEmpty ?: 2
//                AppConfig.REWARD_DAILY = config.rewardDaily ?: 5
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }


//    fun fetch(onComplete: () -> Unit) {
//        mConfigLoader.fetch(onComplete)
//    }

}