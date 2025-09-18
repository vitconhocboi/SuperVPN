package com.core.billing

interface ProductIdProvider {
    fun subscriptionProducts(): List<String>
    fun inAppProducts(): List<String>
    fun autoConsumeProducts(): Set<String>
}