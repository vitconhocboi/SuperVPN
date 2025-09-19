package com.tici.vpn.proxy.master.required.inapp

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.core.baseui.BillingViewModel
import com.core.billing.BillingManager
import com.core.billing.ProductIdProvider
import com.core.billing.model.BillingModel
import com.core.preference.PurchasePreferences
import com.core.utilities.util.Timber
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InAppBillingViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    val billingManager: BillingManager,
    private val productIdProvider: ProductIdProvider,
    private val purchasePreferences: PurchasePreferences
): BillingViewModel, ViewModel() {

    private val _productVipLifeTime: MutableStateFlow<BillingModel.OneTimePurchaseProduct?> = MutableStateFlow(null)
    override val productVipLifeTime: StateFlow<BillingModel.OneTimePurchaseProduct?>
        get() = _productVipLifeTime

    private val _productYearly = MutableStateFlow<BillingModel.SubscriptionProduct?>(null)
    override val productYearly: StateFlow<BillingModel.SubscriptionProduct?>
        get() = _productYearly

    private val _productMonthly = MutableStateFlow<BillingModel.SubscriptionProduct?>(null)
    override val productMonthly: StateFlow<BillingModel.SubscriptionProduct?>
        get() = _productMonthly

    private val _productWeekly = MutableStateFlow<BillingModel.SubscriptionProduct?>(null)
    override val productWeekly: StateFlow<BillingModel.SubscriptionProduct?>
        get() = _productWeekly

    private val _vipState = MutableStateFlow<Result<Boolean>?>(null)
    override val vipState: StateFlow<Result<Boolean>?>
        get() = _vipState

    private val _restorePurchaseState = MutableSharedFlow<Result<Boolean>?>()
    override val restorePurchaseState: SharedFlow<Result<Boolean>?>
        get() = _restorePurchaseState
    private val _newPurchaseProLifeTime = MutableStateFlow<String?>(null)
    override val newPurchaseProLifeTime: StateFlow<String?>
        get() = _newPurchaseProLifeTime


    private val _newPurchaseYearly = MutableStateFlow<String?>(null)
    override val newPurchaseYearly: StateFlow<String?>
        get() = _newPurchaseYearly

    private val _newPurchaseMonthly = MutableStateFlow<String?>(null)

    override val newPurchaseMonthly: StateFlow<String?>
        get() = _newPurchaseMonthly

    private val _newPurchaseWeekly = MutableStateFlow<String?>(null)
    override val newPurchaseWeekly: StateFlow<String?>
        get() = _newPurchaseWeekly

    private val _newPurchaseAny = MutableStateFlow<List<String>?>(null)
    override val newPurchaseAny: StateFlow<List<String>?>
        get() = _newPurchaseAny

    private val _checkReadyState = MutableStateFlow<Boolean?>(null)
    override val checkReadyState: StateFlow<Boolean?>
        get() = _checkReadyState

    init {
        viewModelScope.launch {
            launch {
                billingManager.newPurchaseFlow.collect {
                    if (it.contains(ProductIdProviderImpl.Companion.PRO)) {

                        purchasePreferences.isProLifeTime = true
                        _newPurchaseProLifeTime.emit(ProductIdProviderImpl.Companion.PRO)

                    } else if (it.contains(ProductIdProviderImpl.Companion.PRO_BY_YEAR)) {

                        purchasePreferences.isProByYear = true
                        _newPurchaseYearly.emit(ProductIdProviderImpl.Companion.PRO_BY_YEAR)

                    } else if (it.contains(ProductIdProviderImpl.Companion.PRO_BY_MONTH)) {

                        purchasePreferences.isProByMonth = true
                        _newPurchaseMonthly.emit(ProductIdProviderImpl.Companion.PRO_BY_MONTH)

                    } else if (it.contains(ProductIdProviderImpl.Companion.PRO_BY_WEEK)) {

                        purchasePreferences.isProByWeek = true
                        _newPurchaseWeekly.emit(ProductIdProviderImpl.Companion.PRO_BY_WEEK)

                    }
                    _newPurchaseAny.emit(it)
                    restorePurchased(requiredRefresh = false, isNotifyRestore = false)
                    Timber.Forest.d("newPurchaseFlow $it")
                   /* if(BuildConfig.DEBUG) {
                        Toast.makeText(appContext, "newPurchaseFlow $it", Toast.LENGTH_SHORT).show()
                    }*/
                }
            }

            launch {
                billingManager.refresh()
            }

            launch {
                billingManager.purchaseConsumedFlow.collect {
                    Timber.Forest.d("purchaseConsumedFlow: $it")
                }
            }

            launch {
                billingManager.billingRefreshFlow.collect {
                    Timber.Forest.d("billingRefreshFlow: $it")
                    if (it) {
                        if(purchasePreferences.isUserVip()) {
                            restorePurchased(requiredRefresh = false, isNotifyRestore = false)
                        }
                        loadSubscription(ProductIdProviderImpl.Companion.PRO_BY_WEEK)
                        loadSubscription(ProductIdProviderImpl.Companion.PRO_BY_MONTH)
                        loadSubscription(ProductIdProviderImpl.Companion.PRO_BY_YEAR)
                        loadInAppProduct(ProductIdProviderImpl.Companion.PRO)
                    }
                }
            }
        }

    }

    /**
     * Tải thông tin gói đăng ký.
     * @param id ID của gói đăng ký cần tải.
     * */
    override fun loadSubscription(id: String) {
        viewModelScope.launch {
            Timber.Forest.e("loadSubscription $id")
            billingManager.getPriceSubscription(id)?.collect {
                if (it is BillingModel.SubscriptionProduct) {
                    when (it.productId) {
                        ProductIdProviderImpl.Companion.PRO_BY_YEAR -> {
                            _productYearly.emit(it)
                        }

                        ProductIdProviderImpl.Companion.PRO_BY_MONTH -> {
                            _productMonthly.emit(it)
                        }

                        ProductIdProviderImpl.Companion.PRO_BY_WEEK -> {
                            _productWeekly.emit(it)
                        }
                    }
                }
                Timber.Forest.d("loadSubscription $id: $it")
            }
        }
    }

    /**
     * Tải thông tin sản phẩm trong ứng dụng.
     * @param id ID của sản phẩm trong ứng dụng cần tải.
     * */
    override fun loadInAppProduct(id: String) {
        viewModelScope.launch {
            Timber.Forest.e("loadInAppProduct $id")
            billingManager.getPriceOneTimePurchase(id)?.collect {
                if (it is BillingModel.OneTimePurchaseProduct) {
                    when (it.productId) {
                        ProductIdProviderImpl.Companion.PRO -> {
                            _productVipLifeTime.emit(it)
                        }
                    }
                }
                Timber.Forest.d("loadInAppProduct $id: $it")
            }
        }
    }

    /**
     * Khởi chạy quy trình thanh toán cho một sản phẩm hoặc đăng ký cụ thể.
     * @param activity Hoạt động hiện tại để khởi chạy quy trình thanh toán.
     * @param productOrSubId ID của sản phẩm hoặc đăng ký cần mua. */
    override fun launchBillingFlow(activity: Activity, productOrSubId: String) {
        if (billingManager.isReady()) {
            billingManager.launchBillingFlow(activity, productOrSubId)
        } else {
            viewModelScope.launch {
                _checkReadyState.emit(false)
            }
        }
    }


    /**
     * Khôi phục các giao dịch mua đã mua trước đó.
     * @param requiredRefresh Xác định xem có cần làm mới trạng thái thanh toán trước khi khôi phục hay không.
     * @param isNotifyRestore Xác định xem có cần thông báo cho người dùng về kết quả khôi phục hay không.
     **/
    override fun restorePurchased(requiredRefresh: Boolean, isNotifyRestore: Boolean) {
        viewModelScope.launch {
            if (requiredRefresh) {
                billingManager.refresh()
            }
            if (!billingManager.isReady()) return@launch

            val listInAppLifeTime = billingManager.getPurchases(productIdProvider.inAppProducts().toTypedArray(), BillingClient.ProductType.INAPP)
            if (listInAppLifeTime.isNotEmpty()) {
                listInAppLifeTime.forEach {
                    if (it.isAcknowledged) {
                        if (it.products.contains(ProductIdProviderImpl.Companion.PRO)) {
                            purchasePreferences.isProLifeTime = true
                        }
                    }
                }
            } else {
                purchasePreferences.isProLifeTime = false
            }

            val subscriptions = billingManager.getPurchases(productIdProvider.subscriptionProducts().toTypedArray(), BillingClient.ProductType.SUBS)
            if (subscriptions.isNotEmpty()) {
                subscriptions.forEach {
                    if (it.isAcknowledged) {
                        if (it.products.contains(ProductIdProviderImpl.Companion.PRO_BY_YEAR)) {
                            purchasePreferences.isProByYear = true
                        } else if (it.products.contains(ProductIdProviderImpl.Companion.PRO_BY_MONTH)) {
                            purchasePreferences.isProByMonth = true
                        } else if (it.products.contains(ProductIdProviderImpl.Companion.PRO_BY_WEEK)) {
                            purchasePreferences.isProByWeek = true
                        }
                    }
                }
            } else {
                purchasePreferences.isProByYear = false
                purchasePreferences.isProByMonth = false
                purchasePreferences.isProByWeek = false
            }

            Log.d("TAG5", "InAppPurchaseViewModel_restorePurchase: listInAppLifeTime = $listInAppLifeTime")
            Log.d("TAG5", "InAppPurchaseViewModel_restorePurchase: subscriptions = $subscriptions")
            Log.d("TAG5", "InAppPurchaseViewModel_restorePurchase: isUserVip = " + purchasePreferences.isUserVip())
            _vipState.emit(Result.success(purchasePreferences.isUserVip()))
            if(isNotifyRestore) {
                _restorePurchaseState.emit(Result.success(purchasePreferences.isUserVip()))
            }
        }
    }

    /**
     * Khôi phục giao dịch mua nếu người dùng đã là VIP.
     * */
    override fun restorePurchasedIfVipReady(
        requiredRefresh: Boolean,
        isNotifyRestore: Boolean
    ) {
        if(purchasePreferences.isUserVip()) {
            restorePurchased(requiredRefresh, isNotifyRestore)
        }
    }

    /** Chỉ gọi khi debug test*/
    override fun removeVip() {
        viewModelScope.launch {
            productIdProvider.inAppProducts().forEach {
                billingManager.consumeInAppPurchase(it)
            }
            productIdProvider.subscriptionProducts().forEach {
                billingManager.consumeInAppPurchase(it)
            }
            restorePurchased(requiredRefresh = false, isNotifyRestore = true)
        }
    }
}