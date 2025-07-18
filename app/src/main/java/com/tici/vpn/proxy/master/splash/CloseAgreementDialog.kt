package com.tici.vpn.proxy.master.splash

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.tici.vpn.proxy.master.base.BaseDialog
import com.tici.vpn.proxy.master.databinding.DialogAcceptCollectDataBinding
import com.tici.vpn.proxy.master.databinding.DialogAgreementCloseBinding
import com.tici.vpn.proxy.master.databinding.DialogConfirmDnsSettingBinding

class CloseAgreementDialog(
    activity: FragmentActivity
) : BaseDialog<DialogAgreementCloseBinding>(activity) {

    override fun initUI() {

        binding?.apply {

            btnOk.setOnClickListener {
                dismiss()
            }

            btnCancel.setOnClickListener {
                dismiss()
                activity.finish()
            }
        }
    }

    override fun initViewBinding(activity: Activity): DialogAgreementCloseBinding {
        return DialogAgreementCloseBinding.inflate(LayoutInflater.from(activity), null, false)
    }
}