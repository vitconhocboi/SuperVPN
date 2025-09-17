package com.tici.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.common.baseui.BaseAppConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.tici.vpn.proxy.master.billing.BillingManager
import com.tici.vpn.proxy.master.billing.BillingManager.billingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

//    val isSub = MutableLiveData(false)

    fun setSub(sub: Boolean) {
        BaseAppConfig.isSub = sub
        SharedData.isSub.postValue(sub)
    }

    fun isSub(): Boolean {
        return BaseAppConfig.isSub
    }

//    fun init(context: Context) {
//        BillingManager.init(context)
//    }

    fun checkActiveSubscriptions(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ready = withContext(Dispatchers.IO) {
                ensureBillingReady(context)
            }
            if (!ready) return@launch

            val isSubscribed = withContext(Dispatchers.IO) {
                startBillingConnectionAndQueryPurchases()
            }
//            Log.i("SuperVpn", "subscription isSubscribed $isSubscribed")
            BaseAppConfig.isSub = isSubscribed
//            Log.i("TestRelease", "subscription isSubscribed $isSubscribed")
//            Timber.d("Test_Subscribe isSub postValue $isSubscribed")
            SharedData.isSub.postValue(isSubscribed)
            onResult(BaseAppConfig.isSub)
        }
    }

    suspend fun ensureBillingReady(context: Context): Boolean =
        suspendCancellableCoroutine { cont ->
            BillingManager.init(context) { isReady ->
                cont.resume(isReady)
            }
        }

    private suspend fun startBillingConnectionAndQueryPurchases(): Boolean =
        suspendCancellableCoroutine { cont ->

            if (!billingClient.isReady) {
                // Don't reconnect manually again — wait for `init()` to finish first
//                Log.i("SuperVpn", "BillingClient is not ready. Call init() first and wait.")
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
//                    Log.i(
//                        "SuperVpn",
//                        "subscription billing onBillingSetupFinished ${billingResult.responseCode}"
//                    )
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val params = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()

                        billingClient.queryPurchasesAsync(params) { result, purchasesList ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
//                                Log.i("SuperVpn", "subscription BillingResponseCode OK")
                                val isSubscribed = purchasesList.any { purchase ->
//                                    Log.i("SuperVpn", "TestRelease purchase item ${purchase.purchaseState} ${Purchase.PurchaseState.PURCHASED} ${purchase.isAcknowledged}")
                                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                                            purchase.isAcknowledged
                                }
//                                Log.i("SuperVpn", "subscription status $isSubscribed")
                                cont.resume(isSubscribed)
                            } else {
//                                Log.i("SuperVpn", "subscription BillingResponseCode ${result.responseCode}")
                                cont.resume(false)
                            }
                            // Important: don't call endConnection here, since you might want to keep billingClient alive.
                        }
                    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
//                        Log.i(
//                            "SuperVpn",
//                            "subscription DEVELOPER_ERROR ${billingResult.responseCode}"
//                        )
                        cont.resume(false)
                    } else {
//                        Log.i("SuperVpn", "subscription OTHER ${billingResult.responseCode}")
                        cont.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    cont.resume(false)
                }
            })
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
