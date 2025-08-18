package com.tici.vpn.proxy.master.splash

import android.app.Activity
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.tici.vpn.proxy.master.base.BaseDialog
import com.tici.vpn.proxy.master.databinding.DialogAgreementBinding

class AgreementDialog(
    activity: FragmentActivity,
    val onContinue: () -> Unit
) : BaseDialog<DialogAgreementBinding>(activity) {

    override fun initUI() {
        // Make dialog fullscreen
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent) // optional: transparent background

        binding?.apply {
            btnAccept.setOnClickListener {
                dismiss()
                onContinue()
            }

            btnClose.setOnClickListener {
                CloseAgreementDialog(activity = activity).show()
            }
        }
    }

    override fun initViewBinding(activity: Activity): DialogAgreementBinding {
        return DialogAgreementBinding.inflate(LayoutInflater.from(activity), null, false)
    }
}