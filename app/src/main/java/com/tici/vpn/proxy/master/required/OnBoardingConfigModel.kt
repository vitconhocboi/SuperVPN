package com.tici.vpn.proxy.master.required

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class OnBoardingConfigModel(
    @Json(name = "version")
    var version: Int?
)