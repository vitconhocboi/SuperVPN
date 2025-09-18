package com.core.baseui.customviews.ratingBar

import android.view.MotionEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt


class RatingBarUtils {
    companion object {
        private var mDecimalFormat: DecimalFormat? = null
        private const val MAX_CLICK_DISTANCE = 5
        private const val MAX_CLICK_DURATION = 200

        fun isClickEvent(startX: Float, startY: Float, event: MotionEvent): Boolean {
            val duration = (event.eventTime - event.downTime).toFloat()
            if (duration > MAX_CLICK_DURATION) {
                return false
            }
            val differenceX = Math.abs(startX - event.x)
            val differenceY = Math.abs(startY - event.y)
            return !(differenceX > MAX_CLICK_DISTANCE || differenceY > MAX_CLICK_DISTANCE)
        }

        fun calculateRating(partialView: PartialView, stepSize: Float, eventX: Float): Float {
            val decimalFormat: DecimalFormat? = getDecimalFormat()
            val ratioOfView: Float = decimalFormat?.format((eventX - partialView.left) / partialView.width)?.toFloat() ?: 0F
            val steps = (ratioOfView / stepSize).roundToInt() * stepSize
            return decimalFormat!!.format(partialView.tag as Int - (1 - steps)).toFloat()
        }

        fun getValidMinimumStars(minimumStars: Float, numStars: Int, stepSize: Float): Float {
            var minimumStars = minimumStars
            if (minimumStars < 0) {
                minimumStars = 0f
            }
            if (minimumStars > numStars) {
                minimumStars = numStars.toFloat()
            }
            if (minimumStars % stepSize != 0f) {
                minimumStars = stepSize
            }
            return minimumStars
        }

        private fun getDecimalFormat(): DecimalFormat? {
            if (mDecimalFormat == null) {
                val symbols = DecimalFormatSymbols(Locale.ENGLISH)
                symbols.decimalSeparator = '.'
                mDecimalFormat = DecimalFormat("#.##", symbols)
            }
            return mDecimalFormat
        }
    }
}