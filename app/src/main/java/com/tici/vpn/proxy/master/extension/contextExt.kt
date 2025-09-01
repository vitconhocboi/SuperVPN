package com.tici.vpn.proxy.master.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.TypedValue

fun Context.dp2Px(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)
        .toInt()
}

fun Context.safeGetString(resourceName: String): String? {
    val resId = resources.getIdentifier(resourceName, "string", packageName)
    return if (resId != 0) getString(resId) else null
}

fun Activity.sendMail(content: String) {
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822" // MIME type for email
        putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com")) // Recipients
        putExtra(Intent.EXTRA_SUBJECT, "Subject of the email") // Subject
        putExtra(Intent.EXTRA_TEXT, content) // Body
    }

    if (emailIntent.resolveActivity(this.packageManager) != null) {
        startActivity(Intent.createChooser(emailIntent, "Send mail using..."))
    }
}