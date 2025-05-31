package com.highsecure.vpn.proxy.master.extension

import android.content.Context
import android.util.TypedValue

fun Context.dp2Px(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)
        .toInt()
}