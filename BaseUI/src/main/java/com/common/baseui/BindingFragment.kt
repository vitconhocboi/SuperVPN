package com.common.baseui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.viewbinding.ViewBinding
import com.common.baseui.util.ConnectionLiveData
import com.simple.libads.AdsFragment

abstract class BindingFragment<VB : ViewBinding> : AdsFragment() {

    private val dialogLoadingAd by lazy { DialogShowLoading(requireContext()) }
    private var _binding: VB? = null

    val binding: VB
        get() = _binding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")
    abstract fun bindingProvider(inflater: LayoutInflater, container: ViewGroup?): VB

    protected fun requireBinding(): VB = requireNotNull(_binding)

    open fun isBuyApp(): Boolean {
        return false
    }
    open fun initView(){}

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingProvider(inflater, container)
        return binding.root
    }

    private var mIsNetworkAvailable = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(this) {
            if(mIsNetworkAvailable != it) {
                onNetworkChange(it)
            }
            mIsNetworkAvailable = it
        }
        initView()
    }

    open fun onNetworkChange(isConnect: Boolean) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateStatusBarColor(@ColorRes resColor: Int) {
        (activity as? BindingActivity<*>)?.updateStatusBarColor(resColor)
    }

    fun showLoading(message: String = "Loading...") {
        dialogLoadingAd.showDialog()
        dialogLoadingAd.setMessage(message)
    }

    fun hideLoading() {
        dialogLoadingAd.dismissDialog()
    }


    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isImplementBackPressed) {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                backPressedCallback
            )
            backPressedCallback.isEnabled = true
        }
    }

    protected open val isImplementBackPressed = false

    override fun onPause() {
        super.onPause()
        if (isImplementBackPressed) {
            backPressedCallback.isEnabled = false
        }
    }

    open fun onBackPress() {}
}