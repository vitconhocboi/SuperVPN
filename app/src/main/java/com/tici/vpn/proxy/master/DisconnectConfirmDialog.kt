package com.tici.vpn.proxy.master

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectConfirmBinding

class DisconnectConfirmDialog(val onConfirm: () -> Unit) : BottomSheetDialogFragment() {
    private var _binding: FragmentDisconnectConfirmBinding? = null
    protected val binding: FragmentDisconnectConfirmBinding
        get() = _binding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")


    override fun onStart() {
        super.onStart()
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.apply {
            setLayout(width, height)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun getTheme() = com.common.baseui.R.style.CustomBottomSheetDialogTheme

    fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentDisconnectConfirmBinding {
        return FragmentDisconnectConfirmBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisconnectConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
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