package com.simple.libads

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.simple.libads.databinding.DialogLoadingWhiteBinding


class LoadingBgWhiteDialog(context: Context) : Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen), DialogTitle {

    private val mViewBinding = DialogLoadingWhiteBinding.inflate(LayoutInflater.from(context))

    init {
        val view = mViewBinding.root
        setContentView(view)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    override fun setMessage(message: String) {
        mViewBinding.tvMessage.text = message
    }
}