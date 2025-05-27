package com.akmobile.supervpn.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akmobile.supervpn.databinding.DialogRateCompleteBinding
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogRateComplete : BottomSheetDialogFragment() {
    private var _binding: DialogRateCompleteBinding? = null
    protected val binding: DialogRateCompleteBinding
        get() = _binding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")

    fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): DialogRateCompleteBinding {
        return DialogRateCompleteBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogRateCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvSubmit.setOnClickNoDoubleClick { dismiss() }
            tvSubmit.isSelected = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}