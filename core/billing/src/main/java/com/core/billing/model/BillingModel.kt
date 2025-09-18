package com.core.billing.model

sealed class BillingModel {

    object None : BillingModel()

    data class SubscriptionProduct(
        val productId: String,
        val priceAmountMicros: Long,
        val formattedPrice: String,
        val billingPeriod: String,
        val freeBillingPeriod: String?,
        val offerToken: String
    ) : BillingModel()

    data class OneTimePurchaseProduct(val formattedPrice: String, val productId: String) : BillingModel()
}

fun BillingModel.hasFreeTrial(): Boolean {
    return this is BillingModel.SubscriptionProduct && freeBillingPeriod != null
}

fun BillingModel.getFreeBillingPeriod(): String? {
    return if (this is BillingModel.SubscriptionProduct) freeBillingPeriod else null
}