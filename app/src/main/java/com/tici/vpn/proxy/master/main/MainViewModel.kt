package com.tici.vpn.proxy.master.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.common.baseui.BaseAppConfig
import com.core.preference.PurchasePreferences
import com.tici.vpn.proxy.master.billing.BillingManager
import com.tici.vpn.proxy.master.billing.BillingManager.billingClient
import com.tici.vpn.proxy.master.utils.DeviceIdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MainViewModel @Inject constructor(val purchasePreferences: PurchasePreferences) :
    ViewModel() {

    fun getDeviceId(context: Context): String = DeviceIdProvider.get(context)
}
