package com.tici.vpn.proxy.master.utils

import android.content.Context
import android.content.Intent
import com.tici.vpn.proxy.master.feature.feature_language.ui.LanguageActivity
import com.tici.vpn.proxy.master.ipinfo.IpInfoActivity
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.network.ProxySpeedTest.ProxyConfig
import com.tici.vpn.proxy.master.premium.PremiumActivity
import com.tici.vpn.proxy.master.proxy.ProxyActivity
import com.tici.vpn.proxy.master.settings.dns.DNSActivity

object Navigator {

    fun startMainActivity(context: Context, reconnect: String = "") {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
//            flags =
//                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("state", reconnect)
        })
    }

    fun startLanguageActivity(context: Context, fromSetting: Boolean = false) {
        context.startActivity(
            LanguageActivity.intentStart(
                context,
                fromSetting = fromSetting
            )
        )
    }

    fun startProxyActivity(context: Context, proxyId: String?) {
        context.startActivity(Intent(context, ProxyActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ID", proxyId)
        })
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

    fun startProxyInfoActivity(context: Context, proxy: ProxyConfig?) {
        context.startActivity(Intent(context, IpInfoActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("Proxy", proxy)
        })
    }
}