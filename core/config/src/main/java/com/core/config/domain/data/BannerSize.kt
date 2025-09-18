package com.core.config.domain.data

sealed class BannerSize {

    abstract val key: String

    companion object {

        fun getSizeBy(key: String): BannerSize {
            return when(key) {
                Anchored.key -> Anchored
                Inline.key -> Inline
                StandardMedium.key -> StandardMedium
                StandardLarge.key -> StandardLarge
                else -> Anchored
            }
        }
    }

    object Anchored: BannerSize() {
        override val key = "anchored"
    }

    object Inline: BannerSize() {
        override val key = "inline"
    }

    object StandardMedium: BannerSize() {
        override val key = "standard_medium"
    }

    object StandardLarge: BannerSize() {
        override val key = "standard_large"
    }

}