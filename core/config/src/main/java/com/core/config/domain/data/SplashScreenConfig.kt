package com.core.config.domain.data


data class SplashScreenConfig(

    val maxTimeToWaitAppOpenAd: Long,

    val timeSkipAppOpenAdWhenNotAvailable: Long,

    val adTypeFirstOpen: AdType,

    val adType: AdType,

    val minTimeWaitProgressBeforeShowAd: Long,

    val isEnableRetry: Boolean,

    val maxRetryCount: Int,

    val retryFixedDelay: Long,

    val isLoadBeforeEuConsent: Boolean
    )