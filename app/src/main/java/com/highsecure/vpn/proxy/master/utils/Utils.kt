package com.highsecure.vpn.proxy.master.utils

import com.highsecure.vpn.proxy.master.R

object Utils {
    fun getFlag(country: String): Int {
        return when (country) {
            "vn" -> R.drawable.ic_flag_vietnam
            "us" -> R.drawable.flag_us
            "kr" -> R.drawable.ic_flag_korea
            "arb" -> R.drawable.ic_flag_arab
            "ind" -> R.drawable.ic_flag_india
            "jp" -> R.drawable.ic_flag_japan
            else -> R.drawable.ic_flag_default
        }
    }

    const val BASE_URL = "http://173.212.215.54:5500/"
}