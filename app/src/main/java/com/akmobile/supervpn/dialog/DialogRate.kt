package com.akmobile.pixelart.pixelcolor.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.DialogRateBinding
import com.common.baseui.BindingSheetDialog
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.extension.setVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogRate : BottomSheetDialogFragment() {
    var onDislike: () -> Unit = {}
    var onLike: () -> Unit = {}
    var star = 0


    private var _binding: DialogRateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 85% of screen width
            val height = WindowManager.LayoutParams.WRAP_CONTENT
            bottomSheet?.layoutParams?.width = width
            bottomSheet?.layoutParams?.height = height
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        // Adjust the width of the dialog
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 85% of screen width
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.apply {
            setLayout(width, height)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    fun initView() {
        binding.apply {
            star1.setOnClickNoDoubleClick {
                rate(1)
            }
            star2.setOnClickNoDoubleClick {
                rate(2)
            }
            star3.setOnClickNoDoubleClick {
                rate(3)
            }
            star4.setOnClickNoDoubleClick {
                rate(4)
            }
            star5.setOnClickNoDoubleClick {
                rate(5)
            }
            tvSubmit.setOnClickNoDoubleClick {
                if (star == 5) {
                    dismiss()
                    openAppInStore(requireContext())
                } else {
                    dismiss()
                    DialogFeedback().show(parentFragmentManager, "dialog_rate")
                }
            }
        }
    }

    private fun rate(star: Int) {
        star.also { this.star = it }

        binding.apply {
            star1.isSelected = false
            star2.isSelected = false
            star3.isSelected = false
            star4.isSelected = false
            star5.isSelected = false
        }
        if (star >= 1) {
            binding.apply {
                ivEmotion.setImageResource(R.drawable.emotion1)
                star1.isSelected = true
                tvSubmit.isEnabled = true
                tvSubmit.setText(R.string.text_rate)
                tvSubmit.isSelected = true
                tvSubmit.setText(R.string.text_rate)
                tvTitle.setText(R.string.text_rate_1)
            }
        }

        if (star >= 2) {
            binding.apply {
                ivEmotion.setImageResource(R.drawable.emotion2)
                star2.isSelected = true
            }
        }

        if (star >= 3) {
            binding.apply {
                ivEmotion.setImageResource(R.drawable.emotion3)
                star3.isSelected = true
            }
        }

        if (star >= 4) {
            binding.apply {
                ivEmotion.setImageResource(R.drawable.emotion4)
                star4.isSelected = true
            }
        }

        if (star >= 5) {
            binding.apply {
                ivEmotion.setImageResource(R.drawable.emotion5)
                star5.isSelected = true
                tvTitle.setText(R.string.text_rate_5)
                tvSubmit.setText(R.string.text_rate_app)
            }
        }
        binding.ivEmotion.setVisible(true)
    }

    fun openAppInStore(context: Context) {
        val uri =
            Uri.parse("market://details?id=" + context.packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        val webUri =
            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
        try {
            startActivity(myAppLinkToMarket.apply { setPackage("com.android.vending") })
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                startActivity(webIntent)
            } catch (webException: ActivityNotFoundException) {
                // Notify the user if no browser or Play Store app is available
                Toast.makeText(
                    context,
                    getString(R.string.fb_common_unable_find_market),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}