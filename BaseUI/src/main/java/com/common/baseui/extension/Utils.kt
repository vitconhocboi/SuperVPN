package com.common.baseui.extension

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import timber.log.Timber

object Utils {
    fun postDelay(timeMillisecond: Long, runnable: Runnable) {
        Handler(Looper.getMainLooper()).postDelayed(runnable, timeMillisecond)
    }

    fun convertHexToColor(hex: String?): Int {
        return try {
            Color.parseColor(hex)
        } catch (ex: Exception) {
            Timber.e("convertHexToColor error: $hex")
            Color.WHITE
        }
    }
}