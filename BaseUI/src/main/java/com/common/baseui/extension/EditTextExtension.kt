package com.common.baseui.extension

import android.widget.EditText

fun EditText.moveCursorToEnd() {
    setSelection(text.length)
}