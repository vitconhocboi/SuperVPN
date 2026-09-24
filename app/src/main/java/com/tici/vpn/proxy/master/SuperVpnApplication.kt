package com.tici.vpn.proxy.master

import com.common.baseui.BaseApplication
import com.core.baseui.BaseCoreApplication
import com.core.billing.ProductIdManager
import com.core.preference.PurchasePreferences
import com.core.rate.RateInApp
import com.tici.vpn.proxy.master.settings.adsblock.AdsBlockInterface
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SuperVpnApplication : BaseCoreApplication() {

    @Inject
    lateinit var purchasePreferences: PurchasePreferences

    @Inject
    lateinit var productIdManager: ProductIdManager

    @Inject
    lateinit var adsBlockRepository: AdsBlockInterface

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        instance = this
    }

    override fun initOtherConfig() {
        createOtherShortCut()
        registerKeyVipList()
        BaseApplication.attachInstance(this) // gán thủ công
        RateInApp.instance.registerActivityLifecycle(this)
        seedAdRules()
    }

    /** First launch after install or upgrade: fill the ad-block rule table from the shipped asset. */
    private fun seedAdRules() {
        appScope.launch {
            try {
                adsBlockRepository.seedDefaultsIfNeeded()
            } catch (e: Exception) {
                // Seed flag stays unset, so this retries on the next launch.
                Timber.e(e, "Failed to seed default ad-block rules")
            }
        }
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