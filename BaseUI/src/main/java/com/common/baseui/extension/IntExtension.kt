package com.common.baseui.extension

import android.graphics.Color

fun Int.invertedColor(): Int {
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    val alpha = Color.alpha(this)

    val brightness = red * 0.299 + green * 0.587 + blue * 0.114

    return if (brightness > 186) Color.BLACK else Color.WHITE
    /*

    val invertedRed = 255 - red;
    val invertedGreen = 255 - green;
    val invertedBlue = 255 - blue;
    val color = Color.rgb(invertedRed, invertedGreen,invertedBlue)
    return color*/
}