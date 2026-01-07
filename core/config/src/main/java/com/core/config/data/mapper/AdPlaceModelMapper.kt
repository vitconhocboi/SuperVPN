package com.core.config.data.mapper

import com.core.config.data.model.AdPlaceModel
import com.core.config.domain.data.AdType
import com.core.config.domain.data.AdPlace
import com.core.config.domain.data.AppOpenAdPlace
import com.core.config.domain.data.BannerAdPlace
import com.core.config.domain.data.BannerSize
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAppProviderAdPlaceName
import com.core.config.domain.data.InterstitialAdPlace
import com.core.config.domain.data.NativeAdPlace
import com.core.config.domain.data.NativeTemplateSize
import com.core.config.domain.data.NoneAdPlace
import com.core.config.domain.data.RewardedInterstitialAdPlace
import com.core.config.domain.data.RewardedVideoAdPlace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AdPlaceModelMapper @Inject constructor(
    private val appAdPlaceName: IAppProviderAdPlaceName
): ModelMapper<AdPlaceModel, AdPlace> {

    override fun toData(model: AdPlaceModel): AdPlace {
        val placeName = appAdPlaceName.findAdPlaceName(model.adPlace ?: "") ?: CoreAdPlaceName.fromKey(model.adPlace ?: "")
        val adId = model.adId ?: ""
        val adType = AdType.getAdTypeBy(model.adType ?: "")
        val isEnable = model.isEnable ?: false
        val isAutoLoadAfterDismiss = model.isAutoLoadAfterDismiss ?: true
        val isIgnoreInterval = model.isIgnoreInterval ?: false
        val isTrackingClick = model.isTrackingClick ?: false
        val isTrackingShow = model.isTrackingShow ?: false
        val ctaRadius = model.ctaRadius
        return when(adType) {
            AdType.RewardedVideo -> RewardedVideoAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                isTrackingClick = isTrackingClick,
                isTrackingShow = isTrackingShow
            )
            AdType.RewardedInterstitial -> RewardedInterstitialAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                isTrackingClick = isTrackingClick,
                isTrackingShow = isTrackingShow
            )
            AdType.Interstitial -> InterstitialAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                isTrackingClick = isTrackingClick,
                isTrackingShow = isTrackingShow
            )
            AdType.Native -> NativeAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                nativeTemplateSize = NativeTemplateSize.getSizeBy(model.nativeTemplateSize ?: ""),
                backgroundCta = model.backgroundCta,
                borderColor = model.borderColor,
                backgroundColor = model.backgroundColor,
                primaryTextColor = model.primaryTextColor,
                bodyTextColor = model.bodyTextColor,
                isEnableFullScreenImmersive = model.isEnableFullScreenImmersive,
                isTrackingClick = isTrackingClick,
                ctaTextColor = model.ctaTextColor,
                isTrackingShow = isTrackingShow,
                ctaRadius = ctaRadius,
                backgroundRadius = model.backgroundRadius,
                ctaBorderColor = model.ctaBorderColor,
                backgroundFullColor = model.backgroundFullColor,
                expiredTimeSecond = model.expiredTimeSecond
            )
            AdType.Banner -> BannerAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                bannerSize = BannerSize.getSizeBy(model.bannerSize ?: ""),
                isCollapsible = model.isCollapsible ?: false,
                isTrackingClick = isTrackingClick,
                isTrackingShow = isTrackingShow,
                autoReloadCollapsible = model.autoReloadCollapsible ?: false
            )
            AdType.AppOpen -> AppOpenAdPlace(
                placeName = placeName,
                adId = adId,
                adType = adType,
                isEnable = isEnable,
                isAutoLoadAfterDismiss = isAutoLoadAfterDismiss,
                isIgnoreInterval = isIgnoreInterval,
                limitShow = model.limitShow ?: 10000,
                isTrackingClick = isTrackingClick,
                isTrackingShow = isTrackingShow
            )
            AdType.None -> NoneAdPlace()
        }
    }

}