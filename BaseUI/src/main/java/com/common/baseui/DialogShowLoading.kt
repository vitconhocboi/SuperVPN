package com.common.baseui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.common.baseui.databinding.DialogLoadingAdsBinding

class DialogShowLoading(context: Context) : AlertDialog(context) {


    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(false)
    }

    private lateinit var viewBinding: DialogLoadingAdsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogLoadingAdsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    fun showDialog() {
        if (!isShowing) {
            show()
        }
    }

    fun dismissDialog() {
        if (isShowing) {
            dismiss()
        }
    }

    fun setMessage(message: String) {
        viewBinding.tvContent.text = message
    }
}