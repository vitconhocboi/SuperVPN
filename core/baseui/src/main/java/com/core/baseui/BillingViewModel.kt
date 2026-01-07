package com.core.baseui

import android.app.Activity
import com.core.billing.model.BillingModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingViewModel {
    val productVipLifeTime: StateFlow<BillingModel.OneTimePurchaseProduct?>
    val productYearly: StateFlow<BillingModel.SubscriptionProduct?>
    val productMonthly: StateFlow<BillingModel.SubscriptionProduct?>
    val productWeekly: StateFlow<BillingModel.SubscriptionProduct?>
    val vipState: StateFlow<Result<Boolean>?>

    val newPurchaseProLifeTime: SharedFlow<String?>
    val newPurchaseAny: SharedFlow<List<String>?>
    val newPurchaseYearly: SharedFlow<String?>
    val newPurchaseMonthly: SharedFlow<String?>
    val newPurchaseWeekly: SharedFlow<String?>
    val restorePurchaseState: SharedFlow<Result<Boolean>?>
    val checkReadyState: SharedFlow<Boolean?>

    fun loadSubscription(id: String)
    fun loadInAppProduct(id: String)
    fun launchBillingFlow(activity: Activity, productOrSubId: String)
    fun restorePurchased(requiredRefresh: Boolean, isNotifyRestore: Boolean)
    fun restorePurchasedIfVipReady(requiredRefresh: Boolean, isNotifyRestore: Boolean)
    fun removeVip() //Use for debug
}