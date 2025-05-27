package com.akmobile.supervpn.utils

import android.content.Context
import android.content.Intent
import com.akmobile.supervpn.language.LanguageActivity
import com.akmobile.supervpn.main.MainActivity
import com.akmobile.supervpn.proxy.ProxyActivity
import com.akmobile.supervpn.premium.PremiumActivity
import com.akmobile.supervpn.proxy.DNSActivity
import com.akmobile.supervpn.proxy.ProxyFragment

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
}