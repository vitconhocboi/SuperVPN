package com.tici.vpn.proxy.master.home

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.tici.vpn.proxy.master.base.BaseDialog
import com.tici.vpn.proxy.master.databinding.DialogConfirmDnsSettingBinding

class ConfirmDnsDialog(
    activity: FragmentActivity,
    val onContinue: () -> Unit,
    val selectVpn: () -> Unit,
) : BaseDialog<DialogConfirmDnsSettingBinding>(activity) {

    override fun initUI() {

        val isRTL = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding?.apply {

            btnNext.scaleX = if (isRTL) -1f else 1f

            btnOk.setOnClickListener {
                dismiss()
                onContinue()
            }

            selectVPN.setOnClickListener {
                dismiss()
                selectVpn()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun initViewBinding(activity: Activity): DialogConfirmDnsSettingBinding {
        return DialogConfirmDnsSettingBinding.inflate(LayoutInflater.from(activity), null, false)
    }
}