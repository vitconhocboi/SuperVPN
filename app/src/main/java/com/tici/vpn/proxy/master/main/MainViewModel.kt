package com.tici.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.common.baseui.BaseAppConfig
import com.core.preference.PurchasePreferences
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.tici.vpn.proxy.master.billing.BillingManager
import com.tici.vpn.proxy.master.billing.BillingManager.billingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MainViewModel @Inject constructor(val purchasePreferences: PurchasePreferences) :
    ViewModel() {

    fun isSub(): Boolean {
        return purchasePreferences.isUserVip()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        if (BaseAppConfig.deviceId.isEmpty()) {
            val deviceId = try {
                val adInfo =
                    AdvertisingIdClient.getAdvertisingIdInfo(context)
                if (!adInfo.isLimitAdTrackingEnabled) {
                    adInfo.id
                } else {
                    Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                }
            } catch (e: Exception) {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }
            BaseAppConfig.deviceId = deviceId.toString()
            return deviceId.toString()
        } else {
            return BaseAppConfig.deviceId
        }
    }
}
