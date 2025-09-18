package com.core.config.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RequestConsentConfigModel(

    @Json(name = "is_enable")
    val isEnable: Boolean?,

    @Json(name = "debug_is_eea")
    val debugIsEEA: Boolean?,

    @Json(name = "debug_list_test_device_hashed_id")
    val debugListTestDeviceHashedId: List<String>?,

)