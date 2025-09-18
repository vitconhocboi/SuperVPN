package com.core.baseui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.core.baseui.databinding.DialogAdUnavailableBinding
import com.core.utilities.invisibleIf
import com.core.utilities.setOnClickPreventingDouble
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RetryLoadRewardBottomSheetFragment :
    BaseBottomSheetDialogFragment<DialogAdUnavailableBinding>() {

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogAdUnavailableBinding {
        return DialogAdUnavailableBinding.inflate(inflater, container, false)
    }

    var onRetry: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun initView() {
        isCancelable = false
        dialog?.setCanceledOnTouchOutside(false)

        viewBinding.tvCancel.setOnClickPreventingDouble {
            onCancel?.invoke()
            dismiss()
        }

        viewBinding.tvReconnect.setOnClickPreventingDouble {
            onRetry?.invoke()
            dismiss()
        }
    }

    fun canRetry(isCan: Boolean) {
        runCatching {
            viewBinding.tvReconnect.invisibleIf(!isCan)
            if (!isCan) {
                viewBinding.tvCancel.text = getString(R.string.all_ok)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}