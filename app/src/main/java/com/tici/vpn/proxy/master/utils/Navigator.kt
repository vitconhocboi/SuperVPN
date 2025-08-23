package com.tici.vpn.proxy.master.utils

import android.content.Context
import android.content.Intent
import com.tici.vpn.proxy.master.language.LanguageActivity
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.proxy.ProxyActivity
import com.tici.vpn.proxy.master.premium.PremiumActivity
import com.tici.vpn.proxy.DNSActivity
import com.tici.vpn.proxy.master.ipinfo.IpInfoActivity
import com.tici.vpn.proxy.master.network.ProxySpeedTest.ProxyConfig

object Navigator {

    fun startMainActivity(context: Context, reconnect: String = "") {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            putExtra("state", reconnect)
        })
    }

    fun startLanguageActivity(context: Context, fromSetting : Boolean = false) {
        context.startActivity(Intent(context, LanguageActivity::class.java).apply {
            putExtra("fromSetting", fromSetting)
        })
    }

    fun startProxyActivity(context: Context, proxyId: String?) {
        context.startActivity(Intent(context, ProxyActivity::class.java).apply {
            putExtra("ID", proxyId)
        })
    }

    fun startPremiumActivity(context: Context) {
        context.startActivity(Intent(context, PremiumActivity::class.java))
    }

    fun startDnsActivity(context: Context) {
        context.startActivity(Intent(context, DNSActivity::class.java))
    }

    fun startProxyInfoActivity(context: Context, proxy: ProxyConfig?) {
        context.startActivity(Intent(context, IpInfoActivity::class.java).apply {
            putExtra("Proxy", proxy)
        })
    }
}