package com.core.baseui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.core.baseui.databinding.BaseDialogLoadingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingDialogFragment : BaseDialogFragment<BaseDialogLoadingBinding>() {
    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BaseDialogLoadingBinding {
        return BaseDialogLoadingBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        isCancelable = false
    }

}