package com.highsecure.vpn.proxy.master.home

import android.app.Activity
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.highsecure.vpn.proxy.master.base.BaseDialog
import com.highsecure.vpn.proxy.master.databinding.DialogSettimeBinding

class ChooseTimeDialog(
    activity: FragmentActivity,
    val onSelect: (type: Int) -> Unit
) : BaseDialog<DialogSettimeBinding>(activity) {

    override fun initUI() {
        binding?.apply {

            min60.setOnClickListener {
                if (min60.isChecked) {
                    min90.isChecked = false
                    min120.isChecked = false
                    autoCheckbox.isChecked = false
                }
            }

            min90.setOnClickListener {
                if (min90.isChecked) {
                    min60.isChecked = false
                    min120.isChecked = false
                    autoCheckbox.isChecked = false
                }
            }

            min120.setOnClickListener {
                if (min120.isChecked) {
                    min90.isChecked = false
                    min60.isChecked = false
                    autoCheckbox.isChecked = false
                }
            }

            autoCheckbox.setOnClickListener {
                if (autoCheckbox.isChecked) {
                    min90.isChecked = false
                    min60.isChecked = false
                    min120.isChecked = false
                }
            }

            btnOk.setOnClickListener {
                if (min60.isChecked) {
                    onSelect(60)
                } else if (min90.isChecked) {
                    onSelect(90)
                } else if (min120.isChecked) {
                    onSelect(120)
                } else {
                    onSelect(-1)
                }
                hide()
            }

            btnCancel.setOnClickListener {
                hide()
            }
        }
    }

    override fun initViewBinding(activity: Activity): DialogSettimeBinding {
        return DialogSettimeBinding.inflate(LayoutInflater.from(activity), null, false)
    }
}