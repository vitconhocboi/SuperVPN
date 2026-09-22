package com.core.baseui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.core.baseui.databinding.DialogShowLoadingBinding

class DialogShowLoadingProgress(context: Context) : AlertDialog(context) {


    init {
        window?.setBackgroundDrawableResource(R.color.transparent)
        setCancelable(false)
    }

    private lateinit var viewBinding: DialogShowLoadingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogShowLoadingBinding.inflate(layoutInflater)
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