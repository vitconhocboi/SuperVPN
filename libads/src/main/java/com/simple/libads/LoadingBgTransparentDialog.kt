package com.simple.libads

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.simple.libads.databinding.DialogLoadingTransparentBinding


class LoadingBgTransparentDialog(context: Context) : AlertDialog(context), DialogTitle {

    private val mViewBinding = DialogLoadingTransparentBinding.inflate(LayoutInflater.from(context))

    init {
        setView(mViewBinding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun setMessage(message: String) {
        mViewBinding.tvMessage.text = message
    }
}