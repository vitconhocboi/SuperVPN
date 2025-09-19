package com.core.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.*
import com.core.billing.model.BillingModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * The BillingDataSource implements all billing functionality for our test application.
 * Purchases can happen while in the app or at any time while out of the app, so the
 * BillingDataSource has to account for that.
 *
 * Since every productId (Product ID) can have an individual state, all productId have an associated StateFlow
 * to allow their state to be observed.
 *
 * This BillingDataSource knows nothing about the application; all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 * This code can be reused in a variety of apps.
 *
 * Beginning a purchase flow involves passing an Activity into the Billing Library, but we merely
 * pass it along to the API.
 *
 * This data source has a few automatic features:
 * 1) It checks for a valid signature on all purchases before attempting to acknowledge them.
 * 2) It automatically acknowledges all known productIds for non-consumables, and doesn't set the state
 * to purchased until the acknowledgement is complete.
 * 3) The data source will automatically consume productIds that are set in knownAutoConsumeProducts. As
 * productIds are consumed, a Flow will emit.
 * 4) If the BillingService is disconnected, it will attempt to reconnect with exponential
 * fallback.
 *
 * This data source attempts to keep billing library specific knowledge confined to this file;
 * The only thing that clients of the BillingDataSource need to know are the productIds used by their
 * application.
 *
 * The BillingClient needs access to the Application context in order to bind the remote billing
 * service.
 *
 * The BillingDataSource can also act as a LifecycleObserver for an Activity; this allows it to
 * refresh purchases during onResume.
 */

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes
private const val PRODUCT_DETAILS_RE_QUERY_TIME = 1000L * 60L * 60L * 4L // 4 hours

@Singleton
class BillingDataSource @Inject constructor(
    private val application: Application,
    private val productIdProvider: ProductIdProvider,
) : BillingManager, ProductIdManager, DefaultLifecycleObserver, PurchasesUpdatedListener,
    BillingClientStateListener {

    // Billing client, connection, cached data
    private val billingClient: BillingClient

    private val defaultScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // known ProductId (used to query productId data and validate responses)
    private val knownInAppProducts: MutableList<String> =
        ArrayList(productIdProvider.inAppProducts())
    private val knownSubscriptionProducts: MutableList<String> =
        ArrayList(productIdProvider.subscriptionProducts())

    // Product to auto-consume
    private val knownAutoConsumeProducts: MutableSet<String> = HashSet()

    // how long before the data source tries to reconnect to Google play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    // when was the last successful ProductDetailsResponse?
    private var productDetailsResponseTime = -PRODUCT_DETAILS_RE_QUERY_TIME

    private enum class ProductState {
        PRODUCT_STATE_UN_PURCHASED, PRODUCT_STATE_PENDING, PRODUCT_STATE_PURCHASED, PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
    }

    // Flows that are mostly maintained so they can be transformed into observables.
    private val productStateMap: MutableMap<String, MutableStateFlow<ProductState>> = HashMap()
    private val productDetailsMap: MutableMap<String, MutableStateFlow<ProductDetails?>> = HashMap()

    //region Observables that are used to communicate state.
    private val purchaseConsumptionInProcess: MutableSet<Purchase> = HashSet()
    private val _purchaseConsumedFlow = MutableSharedFlow<List<String>>()
    override val purchaseConsumedFlow = _purchaseConsumedFlow.asSharedFlow()

    private val _newPurchaseFlow = MutableSharedFlow<List<String>>(extraBufferCapacity = 1)
    override val newPurchaseFlow = _newPurchaseFlow.asSharedFlow()

    private val _billingRefreshFlow = MutableStateFlow(false)
    override val billingRefreshFlow = _billingRefreshFlow.asStateFlow()

    private val _billingFlowInProcess = MutableStateFlow(false)
    override val billingFlowInProcess = _billingFlowInProcess.asStateFlow()

    private val _billingFlowCancelled = MutableSharedFlow<Unit>()
    override val billingFlowCancelled = _billingFlowCancelled.asSharedFlow()

    private val _billingFlowFailed = MutableSharedFlow<Unit>()
    override val billingFlowFailed = _billingFlowFailed.asSharedFlow()

    private val _billingFlowServiceError = MutableSharedFlow<Unit>()
    override val billingFlowServiceError = _billingFlowServiceError.asSharedFlow()
    //endregion

    override fun startConnection() {
        if (billingClient.isReady || isRetryingConnection || isConnecting) return
        Log.d(TAG, "startConnection: ")
        isConnecting = true
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d(TAG, "onBillingSetupFinished: ")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "onBillingSetupFinished: OK")
                // The billing client is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.
                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                defaultScope.launch {
                    queryProductDetailsAsync()
                    refreshPurchases()
                }
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.d(TAG, "onBillingSetupFinished: BILLING_UNAVAILABLE")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.d(TAG, "onBillingSetupFinished: DEVELOPER_ERROR")
            }

            else -> retryBillingServiceConnectionWithExponentialBackoff()
        }
        isConnecting = false
    }

    /**
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     */
    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected: ")
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */

    private var isRetryingConnection = false
    private var isConnecting = false

    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        Log.d(TAG, "retryBillingServiceConnectionWithExponentialBackoff: ")
        if (!isRetryingConnection) {
            isRetryingConnection = true
            handler.postDelayed({
                isRetryingConnection = false
                billingClient.startConnection(this@BillingDataSource)
            }, reconnectMilliseconds)
            reconnectMilliseconds =
                min(reconnectMilliseconds * 2, RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
        }
    }

    /**
     * Called by initializeFlows to create the various Flow objects we're planning to emit.
     * @param productList a List<String> of productIds representing purchases and subscriptions.
    </String> */
    private fun addProductFlows(productList: List<String>?) {
        Log.d(TAG, "addProductFlows: $productList")
        productList?.forEach { product ->
            val productState = MutableStateFlow(ProductState.PRODUCT_STATE_UN_PURCHASED)
            val details = MutableStateFlow<ProductDetails?>(null)
            details.subscriptionCount.map { count -> count > 0 } // map count into active/inactive flag
                .distinctUntilChanged() // only react to true<->false changes
                .onEach { isActive -> // configure an action
                    if (isActive && (SystemClock.elapsedRealtime() - productDetailsResponseTime > PRODUCT_DETAILS_RE_QUERY_TIME)) {
                        productDetailsResponseTime = SystemClock.elapsedRealtime()
//                        Log.v(TAG, "ProductDetails not fresh, reQuerying")
                        queryProductDetailsAsync()
                    }
                }
                .launchIn(defaultScope) // launch it
            productStateMap[product] = productState
            productDetailsMap[product] = details
        }
    }

    /**
     * Creates a Flow object for every known productId so the state and productId details can be observed
     * in other layers. The repository is responsible for mapping this data in ways that are more
     * useful for the application.
     */
    private fun initializeFlows() {
        Log.d(TAG, "initializeFlows: ")
        addProductFlows(knownInAppProducts)
        addProductFlows(knownSubscriptionProducts)
    }

    /**
     * Returns whether or not the user has purchased a productId. It does this by returning
     * a Flow that returns true if the productId is in the PURCHASED state and
     * the Purchase has been acknowledged.
     * @return a Flow that observes the productIds purchase state
     */
    override fun isPurchased(productId: String): Flow<Boolean>? {
        Log.d(TAG, "isPurchased: $productId")
        val productStateFLow = productStateMap[productId]
        return productStateFLow?.map { productState -> productState == ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED }
    }

    /**
     * Returns whether or not the user can purchase a productId. It does this by returning
     * a Flow combine transformation that returns true if the productId is in the UNSPECIFIED state, as
     * well as if we have productDetails for the productId. (productIds cannot be purchased without valid
     * ProductDetails.)
     * @return a Flow that observes the productIds purchase state
     */
    override fun canPurchase(productId: String): Flow<Boolean>? {
        Log.d(TAG, "canPurchase: $productId")
        val productDetailsFlow = productDetailsMap[productId]
        val productStateFlow = productStateMap[productId]

        return productDetailsFlow?.let {
            productStateFlow?.combine(it) { productState, productDetails ->
                productState == ProductState.PRODUCT_STATE_UN_PURCHASED && productDetails != null
            }
        }
    }

    // There's lots of information in ProductDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc. You can add to this for your app,
    // or create your own class to pass the information across.
    /**
     * The title of our productId from ProductDetails.
     * @param productId to get the title from
     * @return title of the requested productId as an observable Flow<String>
    </String> */
    override fun getProductTitle(productId: String): Flow<String>? {
        Log.d(TAG, "getProductTitle: $productId")
        val productDetailsFlow = productDetailsMap[productId]
        return productDetailsFlow?.mapNotNull { productDetails ->
            productDetails?.title
        }
    }

    override fun getProductDescription(productId: String): Flow<String>? {
        Log.d(TAG, "getProductDescription: $productId")
        val productDetailsFlow = productDetailsMap[productId]
        return productDetailsFlow?.mapNotNull { productDetails ->
            productDetails?.description
        }
    }

    override fun getPriceSubscription(productId: String): Flow<BillingModel>? {
        Log.d(TAG, "getPriceSubscription: $productId")
        val productDetailsFlow = productDetailsMap[productId]
        return productDetailsFlow?.mapNotNull { productDetails ->
            productDetails?.subscriptionOfferDetails?.let { subscriptionOfferDetails ->
                var hasFreeTrial = false
                subscriptionOfferDetails.getOrNull(0)?.let { subscriptionOfferDetail ->
                    subscriptionOfferDetail.pricingPhases.pricingPhaseList.getOrNull(0)
                        ?.let { pricingPhase ->
                            if (pricingPhase.priceAmountMicros == 0L) {
                                hasFreeTrial = true
                            }
                        }
                }
                if (hasFreeTrial) {
                    subscriptionOfferDetails.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(
                        0
                    )?.let { freeTrial ->
                        subscriptionOfferDetails.getOrNull(1)?.pricingPhases?.pricingPhaseList?.getOrNull(
                            0
                        )?.let {
                            BillingModel.SubscriptionProduct(
                                priceAmountMicros = it.priceAmountMicros,
                                formattedPrice = it.formattedPrice,
                                billingPeriod = it.billingPeriod,
                                freeBillingPeriod = freeTrial.billingPeriod,
                                offerToken = subscriptionOfferDetails[0].offerToken,
                                productId = productDetails.productId,
                                priceCurrencyCode = it.priceCurrencyCode
                            )
                        }
                    }
                } else {
                    subscriptionOfferDetails.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(
                        0
                    )?.let {
                        BillingModel.SubscriptionProduct(
                            priceAmountMicros = it.priceAmountMicros,
                            formattedPrice = it.formattedPrice,
                            billingPeriod = it.billingPeriod,
                            freeBillingPeriod = null,
                            offerToken = subscriptionOfferDetails[0].offerToken,
                            productId = productDetails.productId,
                            priceCurrencyCode = it.priceCurrencyCode
                        )
                    }
                }
            }
        }
    }

    override fun getPriceOneTimePurchase(productId: String): Flow<BillingModel>? {
        Log.d(TAG, "getPriceOneTimePurchase: $productId")
        val productDetailsFlow = productDetailsMap[productId]
        return productDetailsFlow?.mapNotNull { productDetails ->
            productDetails?.oneTimePurchaseOfferDetails?.let { oneTimePurchaseOfferDetails ->
                oneTimePurchaseOfferDetails.formattedPrice.let {
                    BillingModel.OneTimePurchaseProduct(it, productDetails.productId,
                        priceCurrencyCode = oneTimePurchaseOfferDetails.priceCurrencyCode)
                }
            }
        }
    }

    /**
     * Receives the result from [.queryProductDetailsAsync]}.
     *
     * Store the ProductDetails and post them in the [.productDetailsMap]. This allows other
     * parts of the app to use the [ProductDetails] to show productId information and make purchases.
     */
    private fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: List<ProductDetails>?
    ) {
        Log.d(TAG, "onProductDetailsResponse: ")
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
//                Log.i(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
                if (productDetailsList == null || productDetailsList.isEmpty()) {
//                    Log.e(TAG, "onProductDetailsResponse: " + "Found null or empty ProductDetails. " + "Check to see if the productIds you requested are correctly published " + "in the Google Play Console.")
                } else {
                    for (productDetails in productDetailsList) {
                        val productId = productDetails.productId
                        val detailsMutableFlow = productDetailsMap[productId]
                        detailsMutableFlow?.tryEmit(productDetails) ?: Log.e(
                            TAG,
                            "Unknown productId: $productId"
                        )
                    }
                }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
//                Log.e(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
//                Log.i(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
//                defaultScope.launch {
//                    Toast.makeText(application, "Please update your PlayStore app", Toast.LENGTH_LONG).show()
//                }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
//                Log.wtf(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }

            else -> {
//                Log.wtf(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }
        }
        productDetailsResponseTime = if (responseCode == BillingClient.BillingResponseCode.OK) {
            SystemClock.elapsedRealtime()
        } else {
            -PRODUCT_DETAILS_RE_QUERY_TIME
        }
    }

    /**
     * Calls the billing client functions to query product details for both the inApp and subscription
     * productIds. productId details are useful for displaying item names and price lists to the user, and are
     * required to make a purchase.
     */
    private fun queryProductDetailsAsync() {
        Log.d(TAG, "queryProductDetailsAsync: ")
        if (knownInAppProducts.isNotEmpty()) {
            val productList = mutableListOf<QueryProductDetailsParams.Product>()
            knownInAppProducts.forEach {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .setProductId(it)
                        .build()
                )
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList -> // Process the result
                onProductDetailsResponse(billingResult, productDetailsList)
            }
        }
        if (knownSubscriptionProducts.isNotEmpty()) {
            val productList = mutableListOf<QueryProductDetailsParams.Product>()
            knownSubscriptionProducts.forEach {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .setProductId(it)
                        .build()
                )
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList -> // Process the result
                onProductDetailsResponse(billingResult, productDetailsList)
            }
        }
    }

    /*
        GPBLv3 now queries purchases synchronously, simplifying this flow. This only gets active
        purchases.
     */
    private suspend fun refreshPurchases() {
        _billingRefreshFlow.emit(false)
        Log.d(TAG, "refreshPurchases: Starting")

        // In app purchase (one time purchase)
        val inAppPurchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (inAppPurchasesResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(
                TAG,
                "refreshPurchases: Problem getting purchases: " + inAppPurchasesResult.billingResult.debugMessage
            )
        } else {
            processPurchaseList(inAppPurchasesResult.purchasesList, knownInAppProducts)
        }

        // Subscription purchases
        val subsPurchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        if (subsPurchasesResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(
                TAG,
                "refreshPurchases: Problem getting subscriptions: " + subsPurchasesResult.billingResult.debugMessage
            )
        } else {
            processPurchaseList(subsPurchasesResult.purchasesList, knownSubscriptionProducts)
        }

        _billingRefreshFlow.emit(true)
    }

    /**
     * Used internally to get purchases from a requested set of productIds. This is particularly
     * important when changing subscriptions, as onPurchasesUpdated won't update the purchase state
     * of a subscription that has been upgraded from.
     *
     * @param productIds products to get purchase information for
     * @param productType product type, inApp or subscription, to get purchase information for.
     * @return purchases
     */
    override suspend fun getPurchases(
        productIds: Array<String>,
        productType: String
    ): List<Purchase> {
        Log.d(TAG, "getPurchases: $productIds")
        val purchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
        )
        val br = purchasesResult.billingResult
        val returnPurchasesList: MutableList<Purchase> = LinkedList()
        if (br.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Problem getting purchases: " + br.debugMessage)
        } else {
            val purchasesList = purchasesResult.purchasesList
            for (purchase in purchasesList) {
                for (productId in productIds) {
                    for (purchaseProduct in purchase.products) {
                        if (purchaseProduct == productId) {
                            returnPurchasesList.add(purchase)
                        }
                    }
                }
            }
        }
        return returnPurchasesList
    }

    /**
     * Consumes an in-app purchase. Interested listeners can watch the purchaseConsumed LiveEvent.
     * To make things easy, you can send in a list of productId that are auto-consumed by the
     * BillingDataSource.
     */
    override suspend fun consumeInAppPurchase(productId: String) {
        Log.d(TAG, "consumeInAppPurchase: $productId")
        val purchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (purchasesResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Problem getting purchases: " + purchasesResult.billingResult.debugMessage)
        } else {
            for (purchase in purchasesResult.purchasesList) {
                // for right now any bundle of productId must all be consumable
                for (purchaseProduct in purchase.products) {
                    if (purchaseProduct == productId) {
                        consumePurchase(purchase)
                        return
                    }
                }
            }
        }
        Log.e(TAG, "Unable to consume productId: $productId ProductId not found.")
    }

    /**
     * Calling this means that we have the most up-to-date information for a ProductId in a purchase
     * object. This uses the purchase state (Pending, Unspecified, Purchased) along with the
     * acknowledged state.
     * @param purchase an up-to-date object to set the state for the ProductId
     */
    private fun setProductStateFromPurchase(purchase: Purchase) {
        for (purchaseProduct in purchase.products) {
            val productStateFlow = productStateMap[purchaseProduct]
            if (null == productStateFlow) {
                Log.e(
                    TAG,
                    "Unknown ProductId $purchaseProduct. Check to make sure ProductId matches ProductIds in the Play developer console."
                )
            } else {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PENDING -> productStateFlow.tryEmit(ProductState.PRODUCT_STATE_PENDING)
                    Purchase.PurchaseState.UNSPECIFIED_STATE -> productStateFlow.tryEmit(
                        ProductState.PRODUCT_STATE_UN_PURCHASED
                    )

                    Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                        productStateFlow.tryEmit(ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED)
                    } else {
                        productStateFlow.tryEmit(ProductState.PRODUCT_STATE_PURCHASED)
                    }

                    else -> Log.e(TAG, "Purchase in unknown state: " + purchase.purchaseState)
                }
            }
        }
    }

    /**
     * Since we (mostly) are getting ProductId states when we actually make a purchase or update
     * purchases, we keep some internal state when we do things like acknowledge or consume.
     * @param product product ID to change the state of
     * @param newProductState the new state of the ProductId.
     */
    private fun setProductState(product: String, newProductState: ProductState) {
        val productStateFlow = productStateMap[product]
        productStateFlow?.tryEmit(newProductState) ?: Log.e(
            TAG,
            "Unknown ProductId $product. Check to make sure ProductId matches ProductIds in the Play developer console."
        )
    }

    /**
     * Goes through each purchase and makes sure that the purchase state is processed and the state
     * is available through Flows. Verifies signature and acknowledges purchases. PURCHASED isn't
     * returned until the purchase is acknowledged.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * Developers can choose to acknowledge purchases from a server using the
     * Google Play Developer API. The server has direct access to the user database,
     * so using the Google Play Developer API for acknowledgement might be more reliable.
     *
     * If the purchase token is not acknowledged within 3 days,
     * then Google Play will automatically refund and revoke the purchase.
     * This behavior helps ensure that users are not charged unless the user has successfully
     * received access to the content.
     * This eliminates a category of issues where users complain to developers
     * that they paid for something that the app is not giving to them.
     *
     * If a productsToUpdate list is passed-into this method, any purchases not in the list of
     * purchases will have their state set to UN_PURCHASED.
     *
     * @param purchases the List of purchases to process.
     * @param productsToUpdate a list of productIds that we want to update the state from --- this allows us
     * to set the state of non-returned productIds to UN_PURCHASED.
     */
    private fun processPurchaseList(purchases: List<Purchase>?, productsToUpdate: List<String>?) {
        Log.d(TAG, "processPurchaseList: ")
        val updatedProducts = HashSet<String>()
        if (null != purchases) {
            for (purchase in purchases) {
                for (purchaseProduct in purchase.products) {
                    val productStateFlow = productStateMap[purchaseProduct]
                    if (null == productStateFlow) {
                        Log.e(
                            TAG,
                            "processPurchaseList: Unknown productId $purchaseProduct. Check to make sure productId matches productIds in the Play developer console."
                        )
                        continue
                    }
                    updatedProducts.add(purchaseProduct)
                }
                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                val purchaseState = purchase.purchaseState
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // only set the purchased state after we've validated the signature.
                    setProductStateFromPurchase(purchase)
                    var isConsumable = false
                    defaultScope.launch {
                        for (purchaseProduct in purchase.products) {
                            if (knownAutoConsumeProducts.contains(purchaseProduct)) {
                                isConsumable = true
                            } else {
                                if (isConsumable) {
                                    Log.e(
                                        TAG,
                                        "processPurchaseList: Purchase cannot contain a mixture of consumable" + "and non-consumable items: " + purchase.products.toString()
                                    )
                                    isConsumable = false
                                    break
                                }
                            }
                        }
                        if (isConsumable) {
                            consumePurchase(purchase)
                            _newPurchaseFlow.tryEmit(purchase.products)
                        } else if (!purchase.isAcknowledged) {
                            // acknowledge everything --- new purchases are ones not yet acknowledged
                            val billingResult = billingClient.acknowledgePurchase(
                                AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                            )
                            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                Log.e(
                                    TAG,
                                    "processPurchaseList: Error acknowledging purchase: ${purchase.products}"
                                )
                            } else {
                                // purchase acknowledged
                                Log.d(TAG, "processPurchaseList: purchase acknowledged")
                                for (productId in purchase.products) {
                                    setProductState(
                                        productId,
                                        ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
                                    )
                                }
                            }
                            _newPurchaseFlow.tryEmit(purchase.products)
                        }
                    }
                } else {
                    // make sure the state is set
                    setProductStateFromPurchase(purchase)
                }
            }
        } else {
            Log.d(TAG, "Empty purchase list.")
        }
        // Clear purchase state of anything that didn't come with this purchase list if this is
        // part of a refresh.
        if (null != productsToUpdate) {
            for (product in productsToUpdate) {
                if (!updatedProducts.contains(product)) {
                    setProductState(product, ProductState.PRODUCT_STATE_UN_PURCHASED)
                }
            }
        }
    }

    /**
     * Internal call only. Assumes that all signature checks have been completed and the purchase
     * is ready to be consumed. If the productId is already being consumed, does nothing.
     * @param purchase purchase to consume
     */
    private suspend fun consumePurchase(purchase: Purchase) {
        Log.d(TAG, "consumePurchase: ")
        // weak check to make sure we're not already consuming the productId
        if (purchaseConsumptionInProcess.contains(purchase)) {
            // already consuming
            return
        }
        purchaseConsumptionInProcess.add(purchase)
        val consumePurchaseResult = billingClient.consumePurchase(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )

        purchaseConsumptionInProcess.remove(purchase)
        if (consumePurchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Consumption successful. Emitting productId.")
            defaultScope.launch {
                _purchaseConsumedFlow.emit(purchase.products)
            }
            // Since we've consumed the purchase
            for (product in purchase.products) {
                setProductState(product, ProductState.PRODUCT_STATE_UN_PURCHASED)
            }
        } else {
            Log.e(
                TAG,
                "consumePurchase: Error while consuming: ${consumePurchaseResult.billingResult.debugMessage}"
            )
        }
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference. For subscriptions, it supports upgrading from one productId type to another
     * by passing in productIds to be upgraded.
     *
     * @param activity active activity to launch our billing flow from
     * @param productId  (Product ID) to be purchased
     * @return true if launch is successful
     */
    override fun launchBillingFlow(activity: Activity?, productId: String) {
        Log.d(TAG, "launchBillingFlow: ")
        val value = productDetailsMap[productId]?.value
        value?.let { productDetails ->
            val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            when (productDetails.productType) {
                BillingClient.ProductType.SUBS -> {
                    productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken?.let { offerToken ->
                        billingFlowParamsBuilder.setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                        )
                    }
                }

                BillingClient.ProductType.INAPP -> {
                    billingFlowParamsBuilder.setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                }
            }

            defaultScope.launch {
                activity?.let {
                    val br = billingClient.launchBillingFlow(it, billingFlowParamsBuilder.build())
                    if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                        _billingFlowInProcess.value = true
                    } else {
                        Log.e(TAG, "Billing failed: + " + br.debugMessage)
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "ProductDetails not found for: $productId")
            defaultScope.launch {
                _billingFlowServiceError.emit(Unit)
            }
        }
    }

    /**
     * launchBillingFlow.
     * @param billingResult result of the purchase flow.
     * @param list of new purchases.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: $billingResult")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (null != list) {
                processPurchaseList(list, null)
            } else {
                Log.d(TAG, "Null Purchase List Returned from OK response!")
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
                defaultScope.launch {
                    _billingFlowCancelled.emit(Unit)
                }
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                Log.i(TAG, "onPurchasesUpdated: NETWORK_ERROR")
                defaultScope.launch {
                    _billingFlowFailed.emit(Unit)
                }
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.i(
                    TAG,
                    "onPurchasesUpdated: BILLING_UNAVAILABLE || SERVICE_DISCONNECTED || SERVICE_UNAVAILABLE"
                )
                defaultScope.launch {
                    _billingFlowServiceError.emit(Unit)
                }
            }


            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.i(TAG, "onPurchasesUpdated: The user already owns this item")
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
                TAG,
                "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The productId product ID must match and the APK you " +
                        "are using must be signed with release keys."
            )

            else -> Log.d(
                TAG,
                "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage
            )
        }

        Log.d(TAG, "onPurchasesUpdated: not in billing flow")
        _billingFlowInProcess.value = false
    }

    /**
     * It's recommended to reQuery purchases during onResume.
     */
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // this just avoids an extra purchase refresh after we finish a billing flow
        if (!billingFlowInProcess.value) {
            refresh()
        }
    }

    override fun isReady(): Boolean {
        return billingClient.isReady
    }

    override fun refresh() {
        if (billingClient.isReady) {
            defaultScope.launch {
                refreshPurchases()
            }
        } else {
            startConnection()
        }
    }

    override fun changeProductIdList(
        knownInAppProducts: MutableList<String>?,
        knownSubscriptionProducts: MutableList<String>?,
        knownAutoConsumeProducts: MutableSet<String>?
    ) {
        knownInAppProducts?.let {
            this.knownInAppProducts.clear()
            this.knownInAppProducts.addAll(it)
        }
        knownSubscriptionProducts?.let {
            this.knownSubscriptionProducts.clear()
            this.knownSubscriptionProducts.addAll(it)
        }
        knownAutoConsumeProducts?.let {
            this.knownAutoConsumeProducts.clear()
            this.knownAutoConsumeProducts.addAll(it)
        }

        refresh()
    }

    companion object {
        const val TAG = "BillingManager"
        /* const val PRO = "pro"
         const val PRO_BY_YEAR = "pro_by_year"
         const val PRO_BY_MONTH = "pro_by_month"

         const val PRODUCT_ID_IN_APP_PRO_LIFE_TIME = "pro"

         const val PRODUCT_ID_SUBSCRIPTION_BY_YEAR = "pro_by_year"

         const val PRODUCT_ID_SUBSCRIPTION_BY_MONTH = "pro_by_month"

         const val PRODUCT_ID_SUBSCRIPTION_BY_WEEK = "pro_by_week"

         const val PRODUCT_ID_SUPPORT_TOOLS_1 = "support_tools_1"
         const val PRODUCT_ID_SUPPORT_TOOLS_2 = "support_tools_2"
         const val PRODUCT_ID_PACKAGE_KEY_1 = "key_1"
         const val PRODUCT_ID_PACKAGE_KEY_3 = "key_3"
         const val PRODUCT_ID_VIP_FOREVER_10 = "key_10"


         // Sản phẩm tiêu dùng hoặc mau vip 1 lần
         val IN_APP_PRODUCTS = arrayOf<String>(
             PRODUCT_ID_IN_APP_PRO_LIFE_TIME,
             PRODUCT_ID_SUPPORT_TOOLS_1,
             PRODUCT_ID_SUPPORT_TOOLS_2,
             PRODUCT_ID_PACKAGE_KEY_1,
             PRODUCT_ID_PACKAGE_KEY_3,
             PRODUCT_ID_VIP_FOREVER_10 // mua vip 1 lần thì remove khỏi list AUTO_CONSUME_PRODUCTS
         )

         // Subscription products
         val SUBSCRIPTION_PRODUCTS = arrayOf(
             PRODUCT_ID_SUBSCRIPTION_BY_YEAR,
             PRODUCT_ID_SUBSCRIPTION_BY_MONTH,
             PRODUCT_ID_SUBSCRIPTION_BY_WEEK
         )

         // Tự động xóa sản phẩm sau khi sử dụng
         val AUTO_CONSUME_PRODUCTS = arrayOf<String>(
             PRODUCT_ID_SUPPORT_TOOLS_1,
             PRODUCT_ID_SUPPORT_TOOLS_2,
             PRODUCT_ID_PACKAGE_KEY_1,
             PRODUCT_ID_PACKAGE_KEY_3,
 //            PRODUCT_ID_VIP_FOREVER_10
         )*/

        private val handler = Handler(Looper.getMainLooper())
    }

    init {
        knownAutoConsumeProducts.addAll(productIdProvider.autoConsumeProducts())
        initializeFlows()
        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}