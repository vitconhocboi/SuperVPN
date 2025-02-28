package com.simple.libads

import androidx.annotation.StringDef
import com.simple.libads.AdType.Companion.INTER
import com.simple.libads.AdType.Companion.OPEN

@StringDef(
    INTER,
    OPEN
)

@Retention(AnnotationRetention.SOURCE)
annotation class AdType {
    companion object {
        const val INTER = "inter"
        const val OPEN = "open"
    }
}