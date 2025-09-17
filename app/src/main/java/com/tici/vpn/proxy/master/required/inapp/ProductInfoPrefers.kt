package com.tici.vpn.proxy.master.required.inapp

import android.content.Context
import com.core.billing.model.BillingModel
import com.core.preference.SharedPrefs
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductInfoPrefers @Inject constructor(@ApplicationContext applicationContext: Context) {
    private val prefs = SharedPrefs(applicationContext, name = "Purchase", gson = Gson())
    var purchaseYearlyCycle by prefs.preferenceNullable<BillingModel.SubscriptionProduct?>(
        defaultValue = null,
        key = KEY_PURCHASE_YEARLY_CYCLE
    )

    var purchaseMonthlyCycle by prefs.preferenceNullable<BillingModel.SubscriptionProduct?>(
        defaultValue = null,
        key = KEY_PURCHASE_MONTHLY_CYCLE
    )

    var purchaseWeeklyCycle by prefs.preferenceNullable<BillingModel.SubscriptionProduct?>(
        defaultValue = null,
        key = KEY_PURCHASE_WEEKLY_CYCLE
    )

    var purchaseOneTime by prefs.preferenceNullable<BillingModel.OneTimePurchaseProduct?>(
        defaultValue = null,
        key = KEY_PURCHASE_ONE_TIME
    )

    companion object {

        const val KEY_PURCHASE_YEARLY_CYCLE = "KEY_PURCHASE_YEARLY_CYCLE"

        const val KEY_PURCHASE_MONTHLY_CYCLE = "KEY_PURCHASE_MONTHLY_CYCLE"

        const val KEY_PURCHASE_WEEKLY_CYCLE = "KEY_PURCHASE_WEEKLY_CYCLE"

        const val KEY_PURCHASE_ONE_TIME = "KEY_PURCHASE_ONE_TIME"
    }
}