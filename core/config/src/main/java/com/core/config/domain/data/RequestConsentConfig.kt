package com.core.config.domain.data

data class RequestConsentConfig(

    val isEnable: Boolean,

    val debugIsEEA: Boolean,

    val debugListTestDeviceHashedId: List<String>,

)