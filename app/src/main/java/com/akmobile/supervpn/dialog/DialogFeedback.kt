package com.akmobile.pixelart.pixelcolor.dialog

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.akmobile.supervpn.databinding.DialogRateFeedbackBinding
import com.common.baseui.BindingSheetDialog
import com.common.baseui.extension.setOnClickNoDoubleClick

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

    override fun initView() {
        binding.apply {
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
                DialogRateComplete().show(parentFragmentManager, "dialog_rate")
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
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // MIME type for email
            putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com")) // Recipients
            putExtra(Intent.EXTRA_SUBJECT, "Subject of the email") // Subject
            putExtra(Intent.EXTRA_TEXT, content) // Body
        }

        if (activity?.packageManager?.let { emailIntent.resolveActivity(it) } != null) {
            startActivity(Intent.createChooser(emailIntent, "Send mail using..."))
        }
    }
}