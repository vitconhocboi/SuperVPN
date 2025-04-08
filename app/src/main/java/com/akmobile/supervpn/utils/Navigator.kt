package com.akmobile.supervpn.utils

import android.content.Context
import android.content.Intent
import com.akmobile.supervpn.LanguageActivity
import com.akmobile.supervpn.MainActivity
import com.akmobile.supervpn.ProxyActivity
import com.akmobile.supervpn.premium.PremiumActivity
import com.akmobile.supervpn.proxy.ProxyFragment

object Navigator {

    fun startMainActivity(context: Context, proxyId: String?) {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            putExtra("ID", proxyId)
        })
    }

    fun startLanguageActivity(context: Context) {
        context.startActivity(Intent(context, LanguageActivity::class.java))
    }

    fun startProxyActivity(context: Context, proxyId: String?) {
        context.startActivity(Intent(context, ProxyActivity::class.java).apply {
            putExtra("ID", proxyId)
        })
    }

    fun startPremiumActivity(context: Context) {
        context.startActivity(Intent(context, PremiumActivity::class.java))
    }
}