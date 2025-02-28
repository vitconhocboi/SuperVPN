package com.simple.libads.base.bannerads

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.simple.libads.base.bannerads.BannerAnchored.Companion.TYPE_ANCHORED_BOTTOM
import com.simple.libads.base.bannerads.BannerAnchored.Companion.TYPE_ANCHORED_TOP
import com.simple.libads.base.bannerads.BannerType.Companion.TYPE_COLLAPSIBLE
import com.simple.libads.base.bannerads.BannerType.Companion.TYPE_INLINE_ADAPTIVE

@IntDef(
    TYPE_INLINE_ADAPTIVE,
    TYPE_COLLAPSIBLE
)

@Retention(AnnotationRetention.SOURCE)
annotation class BannerType {
    companion object {
        const val TYPE_INLINE_ADAPTIVE = 0
        const val TYPE_COLLAPSIBLE = 1
    }
}

@StringDef(
    TYPE_ANCHORED_TOP,
    TYPE_ANCHORED_BOTTOM
)

@Retention(AnnotationRetention.SOURCE)
annotation class BannerAnchored {
    companion object {
        const val TYPE_ANCHORED_TOP = "top"
        const val TYPE_ANCHORED_BOTTOM = "bottom"
    }
}