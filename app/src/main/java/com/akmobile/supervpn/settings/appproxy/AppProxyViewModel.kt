package com.akmobile.supervpn.settings.appproxy

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.SingleLiveCallBack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppProxyViewModel @Inject constructor() : ViewModel() {

    val loadingApps =  MutableLiveData(false)


}