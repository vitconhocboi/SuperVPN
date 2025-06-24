package com.tici.vpn.proxy.master.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener

object BillingManager {

    lateinit var billingClient: BillingClient
    private var isBillingReady = false

    fun isBillingInit() = ::billingClient.isInitialized

    fun init(context: Context, listener: PurchasesUpdatedListener) {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(listener)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
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