package com.highsecure.vpn.proxy.master.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.highsecure.vpn.proxy.master.db.VpnDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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