package com.highsecure.vpn.proxy.master.main

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.common.baseui.BaseAppConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.highsecure.vpn.proxy.master.billing.BillingManager
import com.highsecure.vpn.proxy.master.billing.BillingManager.billingClient
import com.highsecure.vpn.proxy.master.network.ProxySpeedTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), PurchasesUpdatedListener {

//    val isSub = MutableLiveData(false)

    fun setSub(sub: Boolean) {
        BaseAppConfig.isSub = sub
        SharedData.isSub.postValue(sub)
    }

    fun isSub(): Boolean {
        return BaseAppConfig.isSub
    }

    fun checkActiveSubscriptions(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (!BillingManager.isBillingInit()) {
                BillingManager.init(context, this@MainViewModel)
            }

            val isSubscribed = withContext(Dispatchers.IO) {
                startBillingConnectionAndQueryPurchases()
            }
//            BaseAppConfig.isSub = isSubscribed
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
                    } else {
                        cont.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    cont.resume(false)
                }
            })
        }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    } else {
                        // Already acknowledged, unlock content if needed
                        Timber.d("Purchase already acknowledged: ${purchase.orderId}")
                        SharedData.isSub.postValue(true)
                        break
                    }
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.i("User canceled the purchase flow.")
        } else {
            Timber.e("Purchase failed with code: ${billingResult.responseCode}, message: ${billingResult.debugMessage}")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                SharedData.isSub.postValue(true)
                Timber.d("Purchase acknowledged successfully: ${purchase.orderId}")
                // Unlock premium features or subscriptions here
            } else {
                SharedData.isSub.postValue(false)
                Timber.e("Failed to acknowledge purchase: ${ackResult.debugMessage}")
            }
        }
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
