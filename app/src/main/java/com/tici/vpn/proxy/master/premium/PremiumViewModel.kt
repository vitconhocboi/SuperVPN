package com.tici.vpn.proxy.master.premium

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.billing.BillingManager
import com.tici.vpn.proxy.master.main.SharedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class PremiumViewModel @Inject constructor(
//    private val apiService: ApiService
) : ViewModel(), PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_NAME = "Premium"
        const val PRODUCT_ID = "premium_access"
    }

    val skus = MutableLiveData<ArrayList<Sku>>()

    val products = HashMap<String, ProductDetails>()

    val isLoading = MutableLiveData<Boolean>()

    var subName : String = ""

    fun loadData() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val productDetailsList = withContext(Dispatchers.IO) {
                queryAvailableSubscriptions(
                    //listOf("weekly", "monthly", "yearly")
                    listOf(PRODUCT_ID)
                )
            }

            // Back on Main thread to interact with UI or log
            val listSkus = ArrayList<Sku>()
            products.clear()
            for (product in productDetailsList) {
                Timber.Forest.d("Billing Found subscription: ${product.name} - ${product.oneTimePurchaseOfferDetails?.priceCurrencyCode}")
                productDetailsList.forEach { productDetails ->
                    // Loop through base plans (weekly, monthly, annual)
                    productDetails.subscriptionOfferDetails?.forEach { offer ->
                        val pricingPhase = offer.pricingPhases.pricingPhaseList.first()
                        val price = pricingPhase.formattedPrice
                        val planId = offer.basePlanId // weekly_plan, monthly_plan, etc.
                        listSkus.add(Sku(planId, price))
                        // Store or show these to user
                    }
                }
                products.put(product.name, product)
//                val offer = product.subscriptionOfferDetails?.firstOrNull()
//                val pricingPhase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
//                val formattedPrice = pricingPhase?.formattedPrice ?: "N/A"
//                listSkus.add(Sku(product.name, formattedPrice))
//                products.put(product.name, product)
//                Timber.d("Formatted Price: $formattedPrice")
            }
            if (listSkus.isEmpty()) {
                listSkus.add(Sku("weekly", "59.000 VND"))
                listSkus.add(Sku("monthly", "109.000 VND"))
                listSkus.add(Sku("yearly", "899.000 VND"))
                skus.postValue(listSkus)
            } else {
                val durationOrder = mapOf(
                    "weekly" to 0,
                    "monthly" to 1,
                    "yearly" to 2
                )
                listSkus.sortBy { sku ->
                    durationOrder[sku.productName] ?: Int.MAX_VALUE
                }
                skus.postValue(listSkus)
            }
            isLoading.postValue(false)
        }
    }

    private suspend fun queryAvailableSubscriptions(productIds: List<String>): List<ProductDetails> {
        return suspendCancellableCoroutine { continuation ->
            if (!BillingManager.isReady()) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val productList = productIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            BillingManager.billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(productDetailsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }
    }

    fun launchSubscription(activity: Activity, productName: String) {
        isLoading.postValue(true)
        BillingManager.setPurchaseListener(this@PremiumViewModel)
        subName = productName
        val productDetail = products.get(productName)
        if (productDetail == null) return
        val offerDetails = productDetail.subscriptionOfferDetails?.firstOrNull()
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetail)
                        .setOfferToken(offerDetails?.offerToken ?: "")
                        .build()
                )
            )
            .build()

        val billingResult = BillingManager.billingClient.launchBillingFlow(activity, billingFlowParams)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.Forest.i("Billing Error launching billing flow: ${billingResult.debugMessage}")
        }
        isLoading.postValue(false)
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
                        Timber.Forest.d("Purchase already acknowledged: ${purchase.orderId}")
                        SharedData.isSub.postValue(true)
                        break
                    }
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.Forest.i("User canceled the purchase flow.")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Timber.Forest.i("User own the item.")
            SharedData.isSub.postValue(true)
            BaseAppConfig.isSub = true
        } else {
            Timber.Forest.e("Purchase failed with code: ${billingResult.responseCode}, message: ${billingResult.debugMessage}")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        BillingManager.billingClient.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                SharedData.isSub.postValue(true)
                BaseAppConfig.isSub = true
//                purchaseListener?.onPurchase(subName)
                Timber.Forest.d("Purchase acknowledged successfully: ${purchase.orderId}")
            } else {
                SharedData.isSub.postValue(false)
//                BaseAppConfig.isSub = true
                Timber.Forest.e("Failed to acknowledge purchase: ${ackResult.debugMessage}")
            }
        }
    }
}