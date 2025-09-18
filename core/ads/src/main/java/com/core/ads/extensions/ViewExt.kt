package com.core.ads.extensions

import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.toColorInt

fun View.updateRadius(radius: Float) {
    val bg = this.background
    if (bg is GradientDrawable) {
        val radiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            radius,
            resources.displayMetrics
        )
        bg.cornerRadius = radiusPx
    }
}

fun View.updateBackgroundColor(colorString: String?) {
    try {
        colorString?.let {
            val bg = this.background
            if (bg is GradientDrawable) {
                bg.setColor(it.toColorInt())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}