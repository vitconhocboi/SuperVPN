package com.akmobile.supervpn.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.db.VpnDatabase
import com.common.baseui.SingleLiveCallBack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val database: VpnDatabase) : ViewModel() {

    val loadingApps = MutableLiveData(false)

    fun loadingApp() {
//        getAllVpnedApp()
        loadingApps.value = true
    }

    fun loadingAppDone() {
        loadingApps.value = false
    }

    fun getAllVpnedApp() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = database.appVpnDao().getAll()
            Timber.d("sizesize ${list.size}")
        }
    }
}