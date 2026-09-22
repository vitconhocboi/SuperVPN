package com.tici.vpn.proxy.master

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.core.baseui.BaseBottomSheetDialogFragment
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectConfirmBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisconnectConfirmDialog(val onConfirm: () -> Unit) :
    BaseBottomSheetDialogFragment<FragmentDisconnectConfirmBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDisconnectConfirmBinding {
        return FragmentDisconnectConfirmBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Giữ độ rộng cũ (cạnh ngắn màn hình - 48dp) như khi còn kế thừa BaseAdsBottomSheetDialogFragment
        val metrics = Resources.getSystem().displayMetrics
        val dialogWidth = minOf(metrics.widthPixels, metrics.heightPixels) -
                resources.getDimensionPixelOffset(com.core.dimens.R.dimen._48dp)
        requireDialog().window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        viewBinding.apply {
            tvCancel.setOnClickListener {
                dismiss()
            }
            tvConfirm.setOnClickListener {
                onConfirm()
                dismiss()
            }
        }
    }

}