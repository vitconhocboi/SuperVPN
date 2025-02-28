package com.hoangsinh.supervpn.utils

import android.content.Context
import android.content.Intent
import com.hoangsinh.supervpn.LanguageActivity
import com.hoangsinh.supervpn.MainActivity

object Navigator {

    fun startMainActivity(context: Context) {
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    fun startLanguageActivity(context: Context) {
        context.startActivity(Intent(context, LanguageActivity::class.java))
    }

}