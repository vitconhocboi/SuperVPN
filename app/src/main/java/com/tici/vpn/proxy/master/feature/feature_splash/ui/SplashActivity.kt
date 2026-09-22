package com.tici.vpn.proxy.master.feature.feature_splash.ui

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.core.baseui.ext.bindLiveData
import com.core.utilities.visible
import com.tici.vpn.proxy.master.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SplashActivity : BaseSplashActivity<ActivitySplashBinding>() {
    private val viewModel by viewModels<SplashLoadDataViewModel>()
    override fun bindingProvider(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    override fun initData() {
        viewModel.initData()

        bindLiveData(viewModel.initData) { isReady ->
            if (isReady) {
                if (!viewModel.isInitData) {
                    onDataReady()
                }
                viewModel.isInitData = true
            }
        }
    }

    override fun showLoading() {
        binding.lottieAnimationLoadingSplash.visible()
    }
}