package com.simple.libads

import kotlin.math.abs

object SessionHelper {
    var INTERVAL_INTERSTITIAL = 0L
    var timeOldShow = 0L

    fun setIntervalInterstitial(timeMillis: Long) {
        INTERVAL_INTERSTITIAL = timeMillis
    }

    fun setTimeShowInterstitial() {
        timeOldShow = System.currentTimeMillis()
    }

    fun isCanShowInterstitial() = abs(System.currentTimeMillis() - timeOldShow) > INTERVAL_INTERSTITIAL
}