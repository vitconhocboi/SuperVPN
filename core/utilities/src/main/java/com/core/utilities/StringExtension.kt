package com.core.utilities

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View

/*
  finds and colors all urls contained in a string
  @param linkColor color for the url default is blue
  @param linkClickAction action to perform when user click that link
 */
fun String.linkify(
    linkColor: Int = Color.BLUE,
    linkClickAction: ((link: String) -> Unit)? = null
): SpannableStringBuilder {
    val builder = SpannableStringBuilder(this)
    val matcher = Patterns.WEB_URL.matcher(this)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        builder.setSpan(ForegroundColorSpan(linkColor), start, end, 0)
        val onClick = object : ClickableSpan() {
            override fun onClick(p0: View) {
                linkClickAction?.invoke(matcher.group())
            }
        }
        builder.setSpan(onClick, start, end, 0)
    }
    return builder
}

fun String.fileNameWithoutExtension(): String {
    return substringBeforeLast('.', this)
}