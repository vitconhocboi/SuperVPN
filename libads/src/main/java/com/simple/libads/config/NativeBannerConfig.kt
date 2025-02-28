package com.simple.libads.config


import com.google.gson.annotations.SerializedName

data class NativeBannerConfig(
    @SerializedName("adnetwork")
    val adnetwork: String,
    @SerializedName("style")
    val style: Int,
    @SerializedName("adid")
    val adid: String,
    @SerializedName("archored")
    val archored: String?= null,
    @SerializedName("other_style")
    val otherStyle: Int ?= null
)