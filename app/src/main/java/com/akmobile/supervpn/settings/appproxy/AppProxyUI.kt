package com.akmobile.supervpn.settings.appproxy

import android.graphics.drawable.Drawable

data class AppProxyUI(
    var packageName: String,
    val image: Drawable,
    val appName: String,
    var allowed: Boolean = false
)