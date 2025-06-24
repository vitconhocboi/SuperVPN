package com.tici.vpn.proxy.master.remoteconfig

import com.google.gson.annotations.SerializedName

data class AdUnitInterAdmobItem (
    @SerializedName("ad_id")
    var adId: String,

    @SerializedName("placement_id")
    var placementIds: List<String>
)



