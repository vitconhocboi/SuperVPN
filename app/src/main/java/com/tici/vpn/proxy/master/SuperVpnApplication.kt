package com.tici.vpn.proxy.master

import com.common.baseui.BaseApplication
import com.core.baseui.BaseCoreApplication
import com.core.billing.ProductIdManager
import com.core.preference.PurchasePreferences
import com.core.rate.RateInApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SuperVpnApplication : BaseCoreApplication() {

    @Inject
    lateinit var purchasePreferences: PurchasePreferences

    @Inject
    lateinit var productIdManager: ProductIdManager

    init {
        instance = this
    }

    override fun initOtherConfig() {
        createOtherShortCut()
        registerKeyVipList()
        BaseApplication.attachInstance(this) // gán thủ công
        RateInApp.instance.registerActivityLifecycle(this)
    }


    private fun createOtherShortCut() {
        //TODO tạo thêm shortcut
    }

    /**
     * Đăng ký các key để xác định userVip của ứng dụng (đây là các key lưu trạng thái mua các gói vip trong ứng dụng)
     */
    private fun registerKeyVipList() {
        /*purchasePreferences.registerKeyVipList(
            keyVipList = mutableListOf(
                KEY_IS_PRO_LIFE_TIME,
                KEY_IS_PRO_BY_YEAR,
                KEY_IS_PRO_BY_MONTH,
                KEY_IS_PRO_BY_WEEK,
            )
        )*/
    }

    companion object {

        lateinit var instance: SuperVpnApplication
    }

}