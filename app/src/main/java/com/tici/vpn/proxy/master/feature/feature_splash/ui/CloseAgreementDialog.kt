package com.tici.vpn.proxy.master.feature.feature_splash.ui

import android.app.Activity
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.tici.vpn.proxy.master.base.BaseDialog
import com.tici.vpn.proxy.master.databinding.DialogAgreementCloseBinding

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