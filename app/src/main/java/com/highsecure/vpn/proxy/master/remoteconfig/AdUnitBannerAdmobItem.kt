package com.highsecure.vpn.proxy.master.remoteconfig

import com.google.gson.annotations.SerializedName

data class AdUnitBannerAdmobItem (
    @SerializedName("ad_id")
    var adId: String,

    @SerializedName("placement_id")
    var placementId: String,

    @SerializedName("ad_network")
    var adNetwork: String,

    @SerializedName("ad_type")
    var adType: Int
)



