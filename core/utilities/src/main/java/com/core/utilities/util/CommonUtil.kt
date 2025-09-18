package com.core.utilities.util

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity

object CommonUtil {

    // android 7 or higher
    fun isApi24orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    // android 8 or higher
    fun isApi26orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    // android 9 or higher
    fun isApi28orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    // android 10 or higher
    fun isApi29orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    // android 11 or higher
    fun isApi30orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun isApi31orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun isApi33orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun getRealScreenWidthAsPixel(activity: Activity): Int {
        return if (isApi31orHigher()) {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getWidthScreen(activity: Activity): Float {
        return if (isApi30orHigher()) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            bounds.width().toFloat()
        } else {
            getRealScreenWidthAsPixel(activity).toFloat()
        }
    }

    fun getScreenHeight(activity: Activity): Int {
        return if (isApi31orHigher()) {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun getAdsWidth(widthAdsContainer: Float, activity: AppCompatActivity): Int {
        val adWidthPixels = if (isApi30orHigher()) {
            getRealScreenWidthAsPixel(activity).toFloat()
        } else {
            getRealScreenWidthAsPixel(activity).toFloat()
        }

        val density = activity.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return adWidth
    }

    /**
     * check Xiaomi 2201117SG
     * start activity
     */
    fun isXiaomi(): Boolean {
        return Build.BRAND.lowercase() == "redmi" || Build.BRAND.lowercase() == "xiaomi"
    }

    /**
     * check Xiaomi 2201117SG
     * start activity
     */
    fun isSamsungBrand(): Boolean {
        val brand = "SAMSUNG"
        val brandName = Build.BRAND.uppercase().trim()
        val isSamsung = brand.equals(brandName)
        return isSamsung
    }

    /**
     * check Xiaomi 2201117SG
     * start activity
     */
    fun isOppoAndroid9(): Boolean {
        val brand = "OPPO"
        val brandName = Build.BRAND.uppercase().trim()
        val isOppo = brand.equals(brandName)
        return isOppo && isApi28orHigher()
    }

    fun isXiaomiAndRedmiBrand(): Boolean {
        val brandRedmi = "redmi".uppercase()
        val brandXiaomi = "xiaomi".uppercase()
        val brandName = Build.BRAND.uppercase().trim()
        return brandRedmi.equals(brandName) || brandXiaomi.equals(brandName)
    }

    /**
     * check HUAWEI
     */
    fun isHuawei(): Boolean {
        return Build.BRAND.uppercase().trim() == "HUAWEI" || Build.BRAND.uppercase().trim() == "HONOR"
    }

    fun isVivoBrand() : Boolean {
        return Build.BRAND.lowercase().trim() == "vivo"
    }

    fun getCDMACountryIso(): String? {
        try {
            // ISO [MCC] = Mobile country code
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)

            // Get homeOperator that contain MCC + MNC
            val homeOperator =
                get.invoke(systemProperties, "ro.cdma.home.operator.numeric") as String?

            // First three characters (MCC) from homeOperator represents the country code
            val mcc = homeOperator?.let {
                try {
                    it.substring(0, 3).toInt()
                } catch (e: Exception) {
                    0
                }
            }

            val code = when (mcc) {
                330 -> {
                    "PR"
                }

                310 -> {
                    "US"
                }

                311 -> {
                    "US"
                }

                312 -> {
                    "US"
                }

                316 -> {
                    "US"
                }

                283 -> {
                    "AM"
                }

                460 -> {
                    "CN"
                }

                455 -> {
                    "MO"
                }

                414 -> {
                    "MM"
                }

                619 -> {
                    "SL"
                }

                450 -> {
                    "KR"
                }

                634 -> {
                    "SD"
                }

                434 -> {
                    "UZ"
                }

                232 -> {
                    "AT"
                }

                204 -> {
                    "NL"
                }

                262 -> {
                    "DE"
                }

                247 -> {
                    "LV"
                }

                255 -> {
                    "UA"
                }

                else -> null
            }

            return code
        } catch (e: Exception) {
            return null
        }
    }

}