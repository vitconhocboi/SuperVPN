package com.core.config.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PreventAdClickConfigModel(

    @Json(name = "max_ad_click_per_session")
    val maxAdClickPerSession: Int?,

    @Json(name = "time_per_session")
    val timePerSession: Long?,

    @Json(name = "time_disable_ads_when_reached_max_ad_click")
    val timeDisableAdsWhenReachedMaxAdClick: Long?,

    )