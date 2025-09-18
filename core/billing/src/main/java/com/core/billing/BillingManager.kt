package com.core.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.core.billing.model.BillingModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {

    val purchaseConsumedFlow: SharedFlow<List<String>>

    val newPurchaseFlow: SharedFlow<List<String>>

    val billingRefreshFlow: StateFlow<Boolean>

    val billingFlowInProcess: StateFlow<Boolean>

    val billingFlowCancelled: SharedFlow<Unit>

    val billingFlowFailed: SharedFlow<Unit>

    val billingFlowServiceError: SharedFlow<Unit>

    fun startConnection()

    fun isReady(): Boolean

    fun refresh()

    fun getPriceSubscription(productId: String): Flow<BillingModel>?

    fun getPriceOneTimePurchase(productId: String): Flow<BillingModel>?

    fun launchBillingFlow(activity: Activity?, productId: String)

    suspend fun getPurchases(productIds: Array<String>, productType: String): List<Purchase>

    suspend fun consumeInAppPurchase(productId: String)

    fun isPurchased(productId: String): Flow<Boolean>?

    fun canPurchase(productId: String): Flow<Boolean>?

    fun getProductTitle(productId: String): Flow<String>?

    fun getProductDescription(productId: String): Flow<String>?
}