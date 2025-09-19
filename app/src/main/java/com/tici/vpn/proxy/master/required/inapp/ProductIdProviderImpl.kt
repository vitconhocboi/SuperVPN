package com.tici.vpn.proxy.master.required.inapp

import com.core.billing.ProductIdProvider
import javax.inject.Inject

class ProductIdProviderImpl @Inject constructor(): ProductIdProvider {
    companion object {
        const val PRO = "key_10"
        const val PRO_BY_YEAR = "yearly"
        const val PRO_BY_MONTH = "monthly"
        const val PRO_BY_WEEK = "weekly"
    }

    /** Bao gồm các gói thuê bao theo tháng, năm, tuần, 3 ngày*/
    override fun subscriptionProducts(): List<String> {
        return listOf(
            PRO_BY_YEAR,
            PRO_BY_MONTH,
            PRO_BY_WEEK
        )
    }

    /** Bao gồm product life time vip hoặc những product mua hàng tiêu dùng*/
    override fun inAppProducts(): List<String> {
        return listOf(
            PRO
        )
    }

    /** Những product mua hàng tiêu dùng sẽ tự động được consume khi đã được sử dụng*/
    override fun autoConsumeProducts(): Set<String> {
        return emptySet()
    }

}