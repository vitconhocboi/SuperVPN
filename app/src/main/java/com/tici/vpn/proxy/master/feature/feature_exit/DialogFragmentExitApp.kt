package com.tici.vpn.proxy.master.feature.feature_exit

import android.graphics.Color
import android.graphics.Point
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import com.core.baseui.BaseDialogFragment
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.databinding.FragmentDialogExitAppBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogFragmentExitApp :
    BaseDialogFragment<FragmentDialogExitAppBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDialogExitAppBinding {
        return FragmentDialogExitAppBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        viewBinding.tvNoExit.setOnSingleClick {
            dismiss()
        }

        viewBinding.tvExitApp.setOnSingleClick {
            ActivityCompat.finishAffinity(requireActivity())
        }
    }

    override fun onResume() {
        val window: Window = dialog?.window!!
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val size = Point()
        val display: Display = window.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.BOTTOM)
        super.onResume()
    }

}