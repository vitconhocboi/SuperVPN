package com.tici.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
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

    fun init(context: Context) {
        BillingManager.init(context)
    }

    fun checkActiveSubscriptions(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (!BillingManager.isBillingInit()) {
                BillingManager.init(context)
            }

            val isSubscribed = withContext(Dispatchers.IO) {
                startBillingConnectionAndQueryPurchases()
            }
            BaseAppConfig.isSub = isSubscribed
            SharedData.isSub.postValue(isSubscribed)
            onResult(BaseAppConfig.isSub)
        }
    }

    private suspend fun startBillingConnectionAndQueryPurchases(): Boolean =
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val params = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()

                        billingClient.queryPurchasesAsync(params) { result, purchasesList ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                val isSubscribed = purchasesList.any { purchase ->
                                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                                            purchase.isAcknowledged
                                }
                                cont.resume(isSubscribed)
                            } else {
                                cont.resume(false)
                            }
                            // Important: don't call endConnection here, since you might want to keep billingClient alive.
                        }
                    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                        cont.resume(true)
                    } else {
                        cont.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    cont.resume(false)
                }
            })
        }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context) : String {
        if (BaseAppConfig.deviceId.isEmpty()) {
            val deviceId =  try {
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
