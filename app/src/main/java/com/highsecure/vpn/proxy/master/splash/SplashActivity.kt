package com.highsecure.vpn.proxy.master.splash

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.common.baseui.BaseAppConfig
import com.highsecure.vpn.proxy.master.base.ProductActivity
import com.highsecure.vpn.proxy.master.databinding.ActivitySplashBinding
import com.highsecure.vpn.proxy.master.language.LangType
import com.highsecure.vpn.proxy.master.utils.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ProductActivity<ActivitySplashBinding>() {
    override fun bindingProvider(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    private fun simulateProgress() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 100) {
                delay(50)  // Wait for 200ms
                progress += 5
                binding.customProgressBar.progress = progress
            }
            nextScreen()
        }
    }

    private fun checkLanguage() {
        val local = try {
            Resources.getSystem().configuration.locales[0]
        } catch (ex: Exception) {
            Resources.getSystem().configuration.locale
        }

        val locale = LangType.values()
            .firstOrNull { it.langCode.isNotEmpty() && local.language.contains(it.langCode) }
        if (BaseAppConfig.languageCode.isEmpty() && locale != null) {
            BaseAppConfig.languageCode = locale.langCode
        }
    }

    private fun nextScreen() {
        if (BaseAppConfig.firstTimeSetup) {
            Navigator.startLanguageActivity(this, fromSetting = false)
        } else {
            Navigator.startMainActivity(this)
        }
        finish()
    }

    override fun initView() {
        checkLanguage()
        simulateProgress()
    }
}