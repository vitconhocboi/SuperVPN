package com.akmobile.supervpn.settings.appproxy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akmobile.supervpn.db.VpnAppItemDB
import com.common.baseui.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppProxyViewModel @Inject constructor(private val appProxyUseCase: AppProxyInterface) :
    ViewModel() {
    val allowApp = MutableStateFlow<ResultData<List<VpnAppItemDB>>>(ResultData.standby())

    val isLoading = MutableLiveData<Boolean>()
    val listApps = MutableLiveData<ArrayList<AppProxyUI>>()

    fun getInstalledAppsWithInternetPermission(baseContext: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val packageManager = baseContext.packageManager
            val list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .mapNotNull { appInfo ->
                    if (packageManager.checkPermission(
                            Manifest.permission.INTERNET, appInfo.packageName
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val appIcon = packageManager.getApplicationIcon(appInfo)
                        AppProxyUI(appInfo.packageName, appIcon, appName)
                    } else null
                } as ArrayList<AppProxyUI>
            list.sortByDescending { it.appName }
            list.reverse()
            listApps.postValue(list)
            isLoading.postValue(false)
        }
    }

    fun getAllowApp() {
        viewModelScope.launch(Dispatchers.IO) {
            appProxyUseCase.getAllowApp().collect {
                allowApp.value = it
            }
        }
    }

    fun setAllowApp(item: AppProxyUI) {
        viewModelScope.launch(Dispatchers.IO) {
            appProxyUseCase.setAllowApp(item)
        }
    }

    fun setAllowApps(items: ArrayList<AppProxyUI>) {
        viewModelScope.launch(Dispatchers.IO) {
            appProxyUseCase.setAllowApps(items)
        }
    }

}