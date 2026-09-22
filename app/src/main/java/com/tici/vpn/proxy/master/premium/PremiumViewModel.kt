package com.tici.vpn.proxy.master.premium

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.api.ApiService
import com.tici.vpn.proxy.master.api.DisconnectResponse
import com.tici.vpn.proxy.master.api.Subscription
import com.tici.vpn.proxy.master.billing.BillingManager
import com.tici.vpn.proxy.master.main.SharedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.resume

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel(), PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_NAME = "Premium"
        const val PRODUCT_ID = "premium_access"
    }

    val skus = MutableLiveData<ArrayList<Sku>>()

    val products = HashMap<String, SubscriptionOfferDetails>()

    val isLoading = MutableLiveData<Boolean>()

    val sub = MutableLiveData<Boolean>()

    var subName : String = ""
    lateinit var productDetailSelected : ProductDetails

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
//                Timber.Forest.d("Billing Found subscription: ${product.name} - ${product.oneTimePurchaseOfferDetails?.priceCurrencyCode}")
                productDetailsList.forEach { productDetails ->
                    // Loop through base plans (weekly, monthly, annual)
                    productDetails.subscriptionOfferDetails?.forEach { offer ->
                        val pricingPhase = offer.pricingPhases.pricingPhaseList.first()
                        val price = pricingPhase.formattedPrice
                        val planId = offer.basePlanId // weekly_plan, monthly_plan, etc.
                        val sku = Sku(planId, price)
                        listSkus.add(sku)
                        products[planId] = offer
                        // Store or show these to user
                    }
                    productDetailSelected = productDetails
                }
//                val offer = product.subscriptionOfferDetails?.firstOrNull()
//                val pricingPhase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
//                val formattedPrice = pricingPhase?.formattedPrice ?: "N/A"
//                listSkus.add(Sku(product.name, formattedPrice))
//                products.put(product.name, product)
//                Timber.d("Formatted Price: $formattedPrice")
            }
            if (listSkus.isEmpty()) {
//                listSkus.add(Sku("weekly", "59.000 VND"))
//                listSkus.add(Sku("monthly", "109.000 VND"))
//                listSkus.add(Sku("yearly", "899.000 VND"))
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
        Log.i("SuperVpn", "Test_sub start queryAvailableSubscriptions")
        return suspendCancellableCoroutine { continuation ->
            Log.i("SuperVpn", "Test_sub start queryAvailableSubscriptions 2")
            if (!BillingManager.isReady()) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            Log.i("SuperVpn", "Querying products: $productIds")

//            Log.i("SuperVpn", "Test_sub start queryAvailableSubscriptions 3")
            val productList = productIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            Log.i("SuperVpn", "Test_sub start queryAvailableSubscriptions 4 ${productList}")
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            BillingManager.billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                Log.i("SuperVpn", "Test_sub start queryProductDetailsAsync ")
                Log.i("SuperVpn", "BillingResult code=${billingResult.responseCode}, msg=${billingResult.debugMessage}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i("SuperVpn", "Test_sub start queryProductDetailsAsync OK ${productDetailsList.size}")
                    continuation.resume(productDetailsList)
                } else {
//                    Log.i("SuperVpn", "Test_sub start queryProductDetailsAsync empty")
                    continuation.resume(emptyList())
                }
            }
        }
    }

    fun launchSubscription(activity: Activity, productName: String) {
        isLoading.postValue(true)
        sub.postValue(false)
        BillingManager.setPurchaseListener(this@PremiumViewModel)
        subName = productName
        val saveOfferDetails = products[productName] ?: return
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetailSelected)
                        .setOfferToken(saveOfferDetails.offerToken)
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
                        sub.postValue(true)
                    } else {
                        // Already acknowledged, unlock content if needed
                        Timber.Forest.d("Purchase already acknowledged: ${purchase.orderId}")
                        SharedData.isSub.postValue(true)
                        sub.postValue(true)
                        break
                    }
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.Forest.i("User canceled the purchase flow.")
//            sub.postValue(true)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Timber.Forest.i("User own the item.")
            SharedData.isSub.postValue(true)
            BaseAppConfig.isSub = true
            sub.postValue(true)
            viewModelScope.launch (Dispatchers.IO) {
                try {
                    val sub = Subscription(user_id = BaseAppConfig.deviceId, pack = subName)
//                    apiService.subscription(sub)
                    val result = subscribeSafe(sub)
                    result
                        .onSuccess { _ ->
                        }
                        .onFailure { error ->
                            throw Exception("Failed to assign proxy: $error")
                        }
                } catch (e: Exception) {
                    Timber.e(e)
                    e.printStackTrace()
                }
            }
        } else {
            Timber.Forest.e("Purchase failed with code: ${billingResult.responseCode}, message: ${billingResult.debugMessage}")
            sub.postValue(true)
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
                viewModelScope.launch (Dispatchers.IO) {
                    try {
                        val sub = Subscription(user_id = BaseAppConfig.deviceId, pack = subName)
//                        apiService.subscription(sub)
                        val result = subscribeSafe(sub)
                        result
                            .onSuccess { _ ->
                            }
                            .onFailure { error ->
                                throw Exception("Failed to assign proxy: $error")
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Timber.Forest.d("Purchase acknowledged successfully: ${purchase.orderId}")
            } else {
                SharedData.isSub.postValue(false)
                BaseAppConfig.isSub = false
                Timber.Forest.e("Failed to acknowledge purchase: ${ackResult.debugMessage}")
            }
        }
    }

    suspend fun subscribeSafe(sub: Subscription): Result<DisconnectResponse> {
        return try {
            val response = apiService.subscription(sub)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: SSLHandshakeException) {
            Result.failure(Exception("SSL Handshake failed"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Please try again later."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No internet connection or DNS resolution failed"))
        } catch (e: IOException) {
            Result.failure(Exception("Network I/O error occurred"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}