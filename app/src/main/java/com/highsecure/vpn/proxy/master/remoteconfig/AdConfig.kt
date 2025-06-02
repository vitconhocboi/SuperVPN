package com.highsecure.vpn.proxy.master.remoteconfig

import com.google.gson.annotations.SerializedName

class AdConfig (
    @SerializedName("inter_without_video")
    var interWithoutVideo: AdUnitInterAdmobItem?,

    @SerializedName("inter_with_video")
    var interWithVideo: AdUnitInterAdmobItem?,

    @SerializedName("reward_ads")
    var rewardAds: AdUnitRewardAdmobItem?,

    @SerializedName("native_ads")
    var nativeAds: List<AdUnitNativeAdmobItem>?,

    @SerializedName("banner_ads")
    var bannerAds: List<AdUnitBannerAdmobItem>?,

    @SerializedName("open_ads")
    var openAds: List<AdUnitAdmobItem>?
)