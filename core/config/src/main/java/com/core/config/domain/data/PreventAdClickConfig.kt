package com.core.config.domain.data

data class PreventAdClickConfig(

    val maxAdClickPerSession: Int,

    val timePerSession: Long,

    val timeDisableAdsWhenReachedMaxAdClick: Long,

    )