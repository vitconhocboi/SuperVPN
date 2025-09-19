package com.core.billing.model

import android.content.Context
import android.text.TextUtils
import com.core.billing.R

sealed class BillingModel {

    object None : BillingModel()

    data class SubscriptionProduct(
        val productId: String,
        val priceAmountMicros: Long,
        val formattedPrice: String,
        val priceCurrencyCode: String,
        val billingPeriod: String,
        val freeBillingPeriod: String?,
        val offerToken: String
    ) : BillingModel() {

        private fun convertPriceVND(formattedPrice: String, priceCurrencyCode: String): String {
            return when (priceCurrencyCode) {
                "VND" -> {
                    try {
                        formattedPrice
                            .replace(" ₫", " VND")
                            .replace("₫", " VND")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        formattedPrice
                    }
                }

                else -> {
                    formattedPrice
                }
            }
        }

        private fun convertBillingPeriod(
            context: Context,
            billingPeriod: String,
            isFreeTrialTime: Boolean = false,
        ): String {
            return if (billingPeriod.length == 3) {
                val timeOf = billingPeriod[1].digitToInt()
                val time = billingPeriod[2].toString()
                when {
                    TextUtils.equals("D", time) -> {
                        if (timeOf > 1) {
                            "$timeOf ${context.getString(R.string.core_text_inapp_day)}"
                        } else {
                            context.getString(R.string.core_text_inapp_days)
                        }
                    }

                    TextUtils.equals("W", time) -> {
                        if (timeOf == 1 && isFreeTrialTime) {
                            "7 ${context.getString(R.string.core_text_inapp_days)}"
                        } else {
                            if (timeOf > 1) {
                                "$timeOf ${context.getString(R.string.core_text_inapp_weeks)}"
                            } else {
                                context.getString(R.string.core_text_inapp_weeks)
                            }

                        }
                    }

                    TextUtils.equals("M", time) -> {
                        if (timeOf > 1) {
                            "$timeOf ${context.getString(R.string.core_text_inapp_months)}"
                        } else {
                            context.getString(R.string.core_text_inapp_month)
                        }
                    }

                    TextUtils.equals("Y", time) -> {
                        if (timeOf > 1) {
                            "$timeOf ${context.getString(R.string.core_text_inapp_years)}"
                        } else {
                            context.getString(R.string.core_text_inapp_year)
                        }
                    }

                    else -> {
                        if (timeOf > 1) {
                            "$timeOf ${context.getString(R.string.core_text_inapp_years)}"
                        } else {
                            context.getString(R.string.core_text_inapp_year)
                        }
                    }
                }
            } else {
                context.getString(R.string.core_text_inapp_year)
            }
        }

        fun formatPriceVND(): String {
            return convertPriceVND(formattedPrice, priceCurrencyCode)
        }

        fun formatBillingPeriod(context: Context): String {
            return convertBillingPeriod(context, billingPeriod)
        }

        fun formatFreeBillingPeriod(context: Context, autoRenewText: Boolean = true): String? {
            if (freeBillingPeriod == null) return null
            val freeTrialBillingCycle = convertBillingPeriod(context, freeBillingPeriod, true)
            if (!autoRenewText) return freeTrialBillingCycle
            return "${context.getString(R.string.core_auto_renew_after)} $freeTrialBillingCycle ${
                context.getString(
                    R.string.core_text_free_trial
                )
            }"
        }
    }

    data class OneTimePurchaseProduct(
        val formattedPrice: String,
        val productId: String,
        val priceCurrencyCode: String
    ) : BillingModel()
}

fun BillingModel.hasFreeTrial(): Boolean {
    return this is BillingModel.SubscriptionProduct && freeBillingPeriod != null
}

fun BillingModel.getFreeBillingPeriod(): String? {
    return if (this is BillingModel.SubscriptionProduct) freeBillingPeriod else null
}
