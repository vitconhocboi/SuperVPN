package com.tici.vpn.proxy.master.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tici.vpn.proxy.master.feature.feature_language.ui.LanguageActivity
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.premium.PremiumActivity
import com.tici.vpn.proxy.master.settings.dns.DNSActivity

object Navigator {

    fun startMainActivity(context: Context, reconnect: String = "") {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("state", reconnect)
        })
    }

    fun navigateToMainActivity(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun startLanguageActivity(context: Context, fromSetting: Boolean = false) {
        context.startActivity(
            LanguageActivity.intentStart(
                context,
                fromSetting = fromSetting
            )
        )
    }

    fun startPremiumActivity(context: Context) {
        context.startActivity(Intent(context, PremiumActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    fun startDnsActivity(context: Context) {
        context.startActivity(Intent(context, DNSActivity::class.java))
    }
}