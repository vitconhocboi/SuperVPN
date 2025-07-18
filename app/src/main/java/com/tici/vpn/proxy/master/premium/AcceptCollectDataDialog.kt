package com.tici.vpn.proxy.master.premium

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.tici.vpn.proxy.master.base.BaseDialog
import com.tici.vpn.proxy.master.databinding.DialogAcceptCollectDataBinding
import com.tici.vpn.proxy.master.databinding.DialogConfirmDnsSettingBinding

class AcceptCollectDataDialog(
    activity: FragmentActivity,
    val onContinue: () -> Unit
) : BaseDialog<DialogAcceptCollectDataBinding>(activity) {

    override fun initUI() {

        binding?.apply {

            btnOk.setOnClickListener {
                dismiss()
                onContinue()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun initViewBinding(activity: Activity): DialogAcceptCollectDataBinding {
        return DialogAcceptCollectDataBinding.inflate(LayoutInflater.from(activity), null, false)
    }
}