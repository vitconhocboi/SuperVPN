package com.tici.vpn.proxy.master.remoteconfig

import com.google.gson.annotations.SerializedName

data class AdUnitAdmobItem (
    @SerializedName("ad_id")
    var adId: String,

    @SerializedName("placement_id")
    var placementId: String
)



