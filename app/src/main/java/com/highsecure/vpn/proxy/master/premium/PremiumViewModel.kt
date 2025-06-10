package com.highsecure.vpn.proxy.master.premium

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.common.baseui.BaseAppConfig
import com.highsecure.vpn.proxy.master.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class PremiumViewModel @Inject constructor() : ViewModel() {

    val skus = MutableLiveData<ArrayList<Sku>>()

    val products = HashMap<String, ProductDetails>()

    val isLoading = MutableLiveData<Boolean>()

    fun loadData() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val productDetailsList = withContext(Dispatchers.IO) {
                queryAvailableSubscriptions(
                    listOf("weekly", "monthly", "yearly")
                )
            }

            // Back on Main thread to interact with UI or log
            val listSkus = ArrayList<Sku>()
            products.clear()
            for (product in productDetailsList) {
                Timber.d("Billing Found subscription: ${product.name} - ${product.oneTimePurchaseOfferDetails?.priceCurrencyCode}")
                val offer = product.subscriptionOfferDetails?.firstOrNull()
                val pricingPhase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
                val formattedPrice = pricingPhase?.formattedPrice ?: "N/A"
                listSkus.add(Sku(product.name, formattedPrice))
                products.put(product.name, product)
                Timber.d("Formatted Price: $formattedPrice")
            }
            if (listSkus.isEmpty()) {
                listSkus.add(Sku("premium_weekly", "59.000 VND"))
                listSkus.add(Sku("premium_monthly", "109.000 VND"))
                listSkus.add(Sku("premium_yearly", "899.000 VND"))
                skus.postValue(listSkus)
            } else {
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
            Timber.i("Billing Error launching billing flow: ${billingResult.debugMessage}")
        }
        isLoading.postValue(false)
    }
}
