package com.highsecure.vpn.proxy.master.dialog

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.highsecure.vpn.proxy.master.databinding.DialogRateFeedbackBinding
import com.common.baseui.BindingSheetDialog
import com.common.baseui.extension.setOnClickNoDoubleClick
import androidx.core.net.toUri

class DialogFeedback : BindingSheetDialog<DialogRateFeedbackBinding>() {
    var isFeature: Boolean = false
    var isCrash: Boolean = false
    var isBug: Boolean = false
    var isOther: Boolean = false

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): DialogRateFeedbackBinding {
        return DialogRateFeedbackBinding.inflate(inflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        // Adjust the width of the dialog
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        dialog?.window?.apply {
            setLayout(width, height)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        DialogRateComplete().show(parentFragmentManager, "dialog_rate")
    }

    override fun initView() {

        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.apply {
            ivBack.scaleX = if (isRTL) -1f else 1f
            tvFeature.setOnClickNoDoubleClick {
                isFeature = !isFeature
                tvFeature.isSelected = !tvFeature.isSelected
            }

            tvCrash.setOnClickNoDoubleClick {
                isCrash = !isCrash
                tvCrash.isSelected = !tvCrash.isSelected
            }

            tvBug.setOnClickNoDoubleClick {
                isBug = !isBug
                tvBug.isSelected = !tvBug.isSelected
            }

            tvOther.setOnClickNoDoubleClick {
                isOther = !isOther
                tvOther.isSelected = !tvOther.isSelected
            }

            tvContent.doOnTextChanged { _: CharSequence?, _: Int, _: Int, _: Int ->
                tvSend.isSelected = tvContent.text?.length!! >= 6
                tvSend.isEnabled = tvContent.text?.length!! >= 6
            }
            ivBack.setOnClickNoDoubleClick {
                dismiss()
//                DialogRateComplete().show(parentFragmentManager, "dialog_rate")
            }

            tvSend.setOnClickNoDoubleClick {
                // Create an intent with action ACTION_SEND
                sendMail(tvContent.text.toString())
                dismiss()
                DialogRateComplete().show(parentFragmentManager, "dialog_rate")
            }
        }
    }

    fun sendMail(content: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "mailto:recipient@example.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Subject of the email")
            putExtra(Intent.EXTRA_TEXT, content)
            setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmailExternal")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Gmail app is not installed.", Toast.LENGTH_SHORT).show()
        }
    }
}
