package com.simple.libads

import android.app.Dialog
import android.content.Context

class DialogLoadingBuilder {
    enum class Type {
        INTER,
        OPEN
    }

    private var mMessageInter: String = "Application is running, please wait."
    private var mMessageOpen: String = "Welcome back to Application"
    fun setMessageInter(message: String) = apply {
        mMessageInter = message
    }

    fun setMessageOpen(message: String) = apply {
        mMessageOpen = message
    }

    fun getOpenMessage() = mMessageOpen
    fun getInterMessage() = mMessageInter

    fun build(context: Context, type: Type): Dialog = if (type == Type.INTER) {
        LoadingBgTransparentDialog(context).apply {
            setMessage(mMessageInter)
        }
    } else {
        LoadingBgWhiteDialog(context).apply {
            setMessage(mMessageOpen)
        }
    }
}