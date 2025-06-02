package com.highsecure.vpn.proxy.master.remoteconfig

import com.google.gson.annotations.SerializedName

data class AdUnitNativeAdmobItem (
    @SerializedName("ad_id")
    var adId: String,

    @SerializedName("placement_id")
    var placementId: String,

    @SerializedName("type")
    var type: Int
)



