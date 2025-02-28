package com.simple.libads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdSize

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(visible: Boolean, invisibleType: Int = View.GONE) {
    this.visibility = if (visible) View.VISIBLE else invisibleType
}

fun postDelay(timeMillisecond: Long, runnable: Runnable) {
    Handler(Looper.getMainLooper()).postDelayed(runnable, timeMillisecond)
}

fun View.setMarginTop(marginTop: Int) {
    val layoutManager = this.layoutParams as? ViewGroup.MarginLayoutParams
    layoutManager?.setMargins(0, marginTop, 0, 0)
    layoutManager?.let {
        this.layoutParams = layoutManager
    }
}

fun Context.getWidthDisplay(): Int {
    return resources.displayMetrics.widthPixels
}

inline val Int.dp: Float get() = AdsApplication.appContext.resources.displayMetrics.density * this

fun Context.dpToPx(dip: Float): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics)
}

fun Activity.getBannerAdaptiveSize(): AdSize {
    val display = windowManager?.defaultDisplay
    val outMetrics = DisplayMetrics()
    display?.getMetrics(outMetrics)

    val density = outMetrics.density

    var adWidthPixels = resources.displayMetrics.widthPixels.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}
