package com.akmobile.supervpn.settings.appproxy

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

}