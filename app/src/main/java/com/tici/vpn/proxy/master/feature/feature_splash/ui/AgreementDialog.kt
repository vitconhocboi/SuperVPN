package com.tici.vpn.proxy.master.feature.feature_splash.ui

import android.R
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(R.color.transparent) // optional: transparent background

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