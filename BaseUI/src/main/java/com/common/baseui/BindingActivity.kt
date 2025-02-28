package com.common.baseui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.simple.libads.config.InterConfig


abstract class BindingActivity<T : ViewBinding> : BaseActivity() {

    abstract fun bindingProvider(inflater: LayoutInflater): T

    protected lateinit var binding: T

    override fun interConfigs(): List<InterConfig>? {
        return null
    }

    override fun interConfigsWithoutVideo(): List<InterConfig>? {
        return null
    }

    override fun rewardConfigs(): List<InterConfig>? {
        return null
    }

    override fun isBuyApp(): Boolean {
        return false
    }

    override fun toastNetworkError() {
        
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (::binding.isInitialized.not()) {
            binding = bindingProvider(layoutInflater)
            setContentView(binding.root)
            initView()
//            makeStatusBarTransparent()
            loadAds()
        }
    }

    fun updateStatusBarColor(color: String?) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
    }

    fun updateStatusBarColor(@ColorRes resColor: Int ) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, resColor)
    }

    abstract fun initView()

    open fun navigationLight() = true
    open fun makeStatusBarTransparent() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            decorView.systemUiVisibility = if(navigationLight()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
            window.insetsController?.show(WindowInsets.Type.navigationBars())
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }



}