package com.simple.libads.config

import com.google.gson.annotations.SerializedName

class InterConfig (
    @SerializedName("adid")
    val adid: String,
    @SerializedName("placement_id")
    val placement_id: String,
    @SerializedName("adnetwork")
    val adnetwork: String
)