package com.core.config.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class IapConfigModel(

    @Json(name = "is_show_in_app_purchase_on_start")
    val isShowIAPOnStart: Boolean?,

    @Json(name = "is_show_in_app_purchase_first_open")
    val isShowIAPFirstOpen: Boolean?,

    @Json(name = "is_show_iap_before_request_permission")
    val isShowIAPBeforeRequestPermission: Boolean?,

    @Json(name = "is_enable_iap_v2")
    val isEnableIapV2: Boolean?,

    @Json(name = "time_wait_to_show_close_icon")
    val timeWaitToShowCloseIcon: Long?,

    @Json(name = "upgrade_premium_disable_by_country")
    val upgradePremiumDisableByCountry: List<String>?
)