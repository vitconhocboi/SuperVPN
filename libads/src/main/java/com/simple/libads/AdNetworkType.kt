package com.simple.libads

import androidx.annotation.StringDef
import com.simple.libads.AdNetworkType.Companion.ADMOB

@StringDef(
    ADMOB
)

@Retention(AnnotationRetention.SOURCE)
annotation class AdNetworkType {
    companion object {
        const val ADMOB = "admob"
    }
}