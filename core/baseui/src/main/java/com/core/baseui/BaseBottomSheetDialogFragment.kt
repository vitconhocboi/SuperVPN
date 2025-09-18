package com.core.baseui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.viewbinding.ViewBinding
import com.core.config.domain.RemoteConfigRepository
import com.core.utilities.util.hideNavigationBarForBottomDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

/**
 * Các class kết thừa cần thêm annotation @AndroidEntryPoint*/
abstract class BaseBottomSheetDialogFragment<VB : ViewBinding>() :
    BottomSheetDialogFragment() {


    private var _viewBinding: VB? = null

    protected val viewBinding
        get() = _viewBinding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")

    protected abstract fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): VB

    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    private val isHideNavigationBar: Boolean by lazy {
        remoteConfigRepository.getAppConfig().isHideNavigationBar
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = bindingProvider(inflater, container)
        return _viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
        initView()

        if (isHideNavigationBar) {
            requireDialog().hideNavigationBarForBottomDialog()
        }
    }

    abstract fun initView()

    private fun setupDialog() {

        // Set transparent background and no title
        requireDialog().window?.let {
            it.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            it.setGravity(Gravity.BOTTOM)
        }

    }
}

fun BottomSheetDialogFragment.safeDismiss() {
    if (isAdded && !requireActivity().isFinishing) {
        dismissAllowingStateLoss()
    }
}

