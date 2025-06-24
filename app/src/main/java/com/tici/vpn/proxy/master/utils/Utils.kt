package com.tici.vpn.proxy.master.utils

import com.tici.vpn.proxy.master.R

object Utils {
    fun getFlag(country: String): Int {
        return when (country) {
            "vn" -> R.drawable.ic_flag_vietnam
            "us" -> R.drawable.flag_us
            "kr" -> R.drawable.ic_flag_korea
            "arb" -> R.drawable.ic_flag_arab
            "ind" -> R.drawable.ic_flag_india
            "jp" -> R.drawable.ic_flag_japan
            "germany" -> R.drawable.ic_flag_germany
            "england" -> R.drawable.ic_flag_england
            "russian" -> R.drawable.ic_flag_russian
            "ukraine" -> R.drawable.ic_flag_ukraina
            "spain" -> R.drawable.ic_flag_spain
            "belgium" -> R.drawable.ic_flag_belgium
            "portugal" -> R.drawable.ic_flag_potuguese
            "italia" -> R.drawable.ic_flag_italia
            "belarus" -> R.drawable.ic_flag_belarus
            "switzerland" -> R.drawable.ic_flag_switzerland
            "poland" -> R.drawable.ic_flag_poland
            "finland" -> R.drawable.ic_flag_finland
            "slovania" -> R.drawable.ic_flag_slovania
            "netherlands" -> R.drawable.ic_flag_netherlands
            "sweden" -> R.drawable.ic_flag_sweden
            "austria" -> R.drawable.ic_flag_austria
            "hungarian" -> R.drawable.ic_flag_hungaria
            "bulgarian" -> R.drawable.ic_flag_bulgaria
            "greece" -> R.drawable.ic_flag_greece
            "moldova" -> R.drawable.ic_flag_moldova
            "serbia" -> R.drawable.ic_flag_serbia
            "norway" -> R.drawable.ic_flag_norway
            "czech" -> R.drawable.ic_flag_czech
            "romania" -> R.drawable.ic_flag_romania
            "australia" -> R.drawable.ic_flag_australia
            "indonesia" -> R.drawable.ic_flag_indonesia
            "china" -> R.drawable.ic_flag_china
            "hongkong" -> R.drawable.ic_flag_hongkong
            "taiwan" -> R.drawable.ic_flag_taiwan
            "singapore" -> R.drawable.ic_flag_singapore
            "philipine" -> R.drawable.ic_flag_philipine
            "malaysia" -> R.drawable.ic_flag_malaysia
            "thailand" -> R.drawable.ic_flag_thailand
            "cambodia" -> R.drawable.ic_flag_cambodia
            "laos" -> R.drawable.ic_flag_laos
            "iran" -> R.drawable.ic_flag_iran
            "iraq" -> R.drawable.ic_flag_iraq
            "turkey" -> R.drawable.ic_flag_turkey
            "pakistan" -> R.drawable.ic_flag_pakistan
            "arab" -> R.drawable.ic_flag_arab
            "uae" -> R.drawable.ic_flag_uae
            "oman" -> R.drawable.ic_flag_oman
            "uzbekistan" -> R.drawable.ic_flag_uzbekistan
            "qatar" -> R.drawable.ic_flag_qatar
            "canada" -> R.drawable.ic_flag_canada
            "mexico" -> R.drawable.ic_flag_mexico
            "brazil" -> R.drawable.ic_flag_brazil
            "ecuador" -> R.drawable.ic_flag_ecuador
            "peru" -> R.drawable.ic_flag_peru
            "colombia" -> R.drawable.ic_flag_colombia
            "argentina" -> R.drawable.ic_flag_argentina
            "chile" -> R.drawable.ic_flag_chile
            "venezuela" -> R.drawable.ic_flag_venezuela
            "bolivia" -> R.drawable.ic_flag_bolivia
            "paraguay" -> R.drawable.ic_flag_paraguay
            "uruguay" -> R.drawable.ic_flag_uruguay
            else -> R.drawable.ic_flag_default
        }
    }

    const val BASE_URL = "http://173.212.215.54:5500/"
}