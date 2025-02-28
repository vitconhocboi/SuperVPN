package com.simple.libads.base.nativeads

import androidx.annotation.IntDef
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_CUSTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MAX_CTA_BOTTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_SMALL_CTA_BOTTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_SMALL_CTA_RIGHT
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_CTA_BOTTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_CTA_RIGHT
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_CTA_TOP
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_CTA_TOP_RIGHT
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_MEDIA_CTA_RIGHT
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_SMALL_CTA_TOP
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_MEDIA_CTA_RIGHT_BOTTOM
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_MEDIA_CTA_TOP
import com.simple.libads.base.nativeads.NativeType.Companion.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM_MAX_ROUND

@IntDef(
    TYPE_SMALL_CTA_BOTTOM,
    TYPE_SMALL_CTA_RIGHT,
    TYPE_SMALL_CTA_TOP,
    TYPE_MEDIUM_CTA_BOTTOM,
    TYPE_MEDIUM_CTA_RIGHT,
    TYPE_MEDIUM_CTA_TOP,
    TYPE_MEDIUM_CTA_TOP_RIGHT,
    TYPE_MEDIUM_NO_ICON_CTA_BOTTOM,
    TYPE_MEDIUM_MEDIA_CTA_RIGHT_BOTTOM,
    TYPE_MEDIUM_MEDIA_CTA_RIGHT,
    TYPE_MEDIUM_MEDIA_CTA_TOP,
    TYPE_MEDIUM_NO_ICON_CTA_BOTTOM_MAX_ROUND,
    TYPE_MAX_CTA_BOTTOM,
    TYPE_CUSTOM
)
@Retention(AnnotationRetention.SOURCE)
annotation class NativeType {
    companion object {
        const val TYPE_SMALL_CTA_BOTTOM = 0
        const val TYPE_SMALL_CTA_RIGHT = 1
        const val TYPE_SMALL_CTA_TOP = 2
        const val TYPE_MEDIUM_CTA_BOTTOM = 3
        const val TYPE_MEDIUM_CTA_RIGHT = 4
        const val TYPE_MEDIUM_CTA_TOP = 5
        const val TYPE_MEDIUM_CTA_TOP_RIGHT = 6
        const val TYPE_MEDIUM_NO_ICON_CTA_BOTTOM = 7
        const val TYPE_MEDIUM_MEDIA_CTA_RIGHT_BOTTOM = 8
        const val TYPE_MEDIUM_MEDIA_CTA_RIGHT = 9
        const val TYPE_MEDIUM_MEDIA_CTA_TOP = 10
        const val TYPE_MEDIUM_NO_ICON_CTA_BOTTOM_MAX_ROUND = 11
        const val TYPE_MAX_CTA_BOTTOM = 12
        const val TYPE_CUSTOM = 999
    }
}