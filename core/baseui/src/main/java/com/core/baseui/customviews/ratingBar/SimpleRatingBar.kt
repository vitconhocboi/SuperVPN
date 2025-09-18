package com.core.baseui.customviews.ratingBar

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange


interface SimpleRatingBar {
    fun setNumStars(numStars: Int)

    fun getNumStars(): Int

    fun setRating(rating: Float)

    fun getRating(): Float

    fun setStarWidth(@IntRange(from = 0) starWidth: Int)

    fun getStarWidth() : Int

    fun setStarHeight(@IntRange(from = 0) starHeight: Int)

    fun getStarHeight(): Int

    fun setStarPadding(ratingPadding: Int)

    fun getStarPadding(): Int

    fun setEmptyDrawable(drawable: Drawable)

    fun setEmptyDrawableRes(@DrawableRes res: Int)

    fun setFilledDrawable(drawable: Drawable)

    fun setFilledDrawableRes(@DrawableRes res: Int)

    fun setMinimumStars(@FloatRange(from = 0.0) minimumStars: Float)

    fun isIndicator(): Boolean

    fun setIsIndicator(indicator: Boolean)

    fun isScrollable(): Boolean

    fun setScrollable(scrollable: Boolean)

    fun isClickable(): Boolean

    fun setClickable(clickable: Boolean)

    fun setClearRatingEnabled(enabled: Boolean)

    fun isClearRatingEnabled(): Boolean

    fun getStepSize(): Float

    fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float)
}