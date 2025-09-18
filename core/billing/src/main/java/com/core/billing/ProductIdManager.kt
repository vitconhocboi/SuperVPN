package com.core.billing

interface ProductIdManager {
    fun changeProductIdList(
        knownInAppProducts: MutableList<String>? = null,
        knownSubscriptionProducts: MutableList<String>? = null,
        knownAutoConsumeProducts: MutableSet<String>? = null
    )
}
