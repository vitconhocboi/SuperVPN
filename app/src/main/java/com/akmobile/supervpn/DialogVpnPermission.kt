package com.akmobile.supervpn

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.akmobile.supervpn.databinding.FragmentDialogVpnPermissionBinding
import com.common.baseui.BindingSheetDialog
import com.common.baseui.extension.setOnClickNoDoubleClick


class DialogVpnPermission : BindingSheetDialog<FragmentDialogVpnPermissionBinding>() {
    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDialogVpnPermissionBinding {
        return FragmentDialogVpnPermissionBinding.inflate(inflater, container, false)
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

    override fun initView() {
        super.initView()
        binding.tvGotIt.setOnClickNoDoubleClick {
            dismiss()
        }
    }
}