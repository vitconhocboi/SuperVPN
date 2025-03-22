package com.akmobile.supervpn.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.SingleLiveCallBack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    val timeOutAds: SingleLiveCallBack<Boolean> = SingleLiveCallBack()

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1_000)
            timeOutAds.postValue(true)
        }
    }

    fun timeOut() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(2_000)
            timeOutAds.postValue(true)
        }
    }
}