package com.simple.libads.config

import com.google.gson.annotations.SerializedName

data class OpenConfig (
    @SerializedName("adid")
    val adid: String,
    @SerializedName("adnetwork")
    val adnetwork: String,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("ad_type")
    val ad_type: String,
    @SerializedName("placement_id")
    val placementId: String
)