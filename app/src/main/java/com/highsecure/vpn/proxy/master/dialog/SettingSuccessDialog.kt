package com.highsecure.vpn.proxy.master.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.highsecure.vpn.proxy.master.databinding.PopupSettingSuccessBinding

class SettingSuccessDialog (
    val titleId: Int,
    val contentId: Int = com.highsecure.vpn.proxy.master.R.string.reconnect_to_take_effect
) : DialogFragment() {

    private var binding: PopupSettingSuccessBinding? = null

    fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): PopupSettingSuccessBinding {
        return PopupSettingSuccessBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = bindingProvider(inflater, container)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(android.view.Gravity.TOP)

            // Set desired margin in pixels
            val marginHorizontal = resources.getDimensionPixelSize(com.highsecure.vpn.proxy.master.R.dimen.margin_medium) // e.g., 32dp

            val width = resources.displayMetrics.widthPixels - (marginHorizontal * 2)
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            attributes = attributes.apply {
                y = 0 // align to top
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    fun initView() {
        binding!!.apply {

            tvTitle.text = requireContext().getString(titleId)

            tvContent.text = requireContext().getString(contentId)

            tvClose.setOnClickListener {
                dismiss()
            }
        }
    }
}
