package com.tici.vpn.proxy.master.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener

object BillingManager {

    lateinit var billingClient: BillingClient
    private var isBillingReady = false

    private var listener: PurchasesUpdatedListener? = null

    fun setPurchaseListener(l: PurchasesUpdatedListener) {
        listener = l
    }

    fun isBillingInit() = ::billingClient.isInitialized

    fun init(context: Context, onReady: (Boolean) -> Unit) {

        if (::billingClient.isInitialized && billingClient.isReady) {
            onReady(true)
            return
        }

        Log.i("SuperVpn", "subscription init billingClient")

        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { result, purchases ->
                listener?.onPurchasesUpdated(result, purchases)
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
                    onReady(isBillingReady)
                    // Now you can query products or purchases
                }
            }

            override fun onBillingServiceDisconnected() {
                isBillingReady = false
                // Retry logic here
            }
        })
    }

    fun isReady(): Boolean = isBillingReady
}