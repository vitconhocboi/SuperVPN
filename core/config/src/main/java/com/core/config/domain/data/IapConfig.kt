package com.core.config.domain.data

import android.content.Context
import com.core.preference.AppPreferences
import com.core.preference.PurchasePreferences
import com.core.utilities.getCountryCode

data class IapConfig(
    val isShowIAPOnStart: Boolean,
    val isShowIAPFirstOpen: Boolean,
    val isShowIAPBeforeRequestPermission: Boolean,
    val isEnableIapV2: Boolean,
    val timeWaitToShowCloseIcon: Long,
    val upgradePremiumDisableByCountry: List<String>
) {

    fun shouldShowIAPAfterPermission(context: Context, appPreferences: AppPreferences, purchasePreferences: PurchasePreferences): Boolean {
        if (purchasePreferences.isUserVip()) {
            return false
        }

        if (isIAPDisableByCountry(context)) {
            return false
        }

        if (isShowIAPBeforeRequestPermission) {
            return false
        }

        if (isShowIAPOnStart) {
            return true
        }

        if (isShowIAPFirstOpen && appPreferences.openAppCount == 1) {
            return true
        }

        return false
    }

    fun shouldShowIAPBeforePermission(context: Context, appPreferences: AppPreferences, purchasePreferences: PurchasePreferences): Boolean {
        if (purchasePreferences.isUserVip()) {
            return false
        }

        if (isIAPDisableByCountry(context)) {
            return false
        }

        if (!isShowIAPBeforeRequestPermission) {
            return false
        }

        if (isShowIAPOnStart) {
            return true
        }

        if (isShowIAPFirstOpen && appPreferences.openAppCount == 1) {
            return true
        }

        return false
    }

    fun isIAPDisableByCountry(context: Context): Boolean {
        return upgradePremiumDisableByCountry.contains(context.getCountryCode())
    }
}