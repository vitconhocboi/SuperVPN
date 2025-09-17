package com.tici.vpn.proxy.master.feature.feature_language.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.utilities.getCurrentLanguageCode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(@ApplicationContext private val context: Context): ViewModel() {
    private val _initDataAndNextScreen = MutableLiveData<Boolean>()
    val initDataAndNextScreen: LiveData<Boolean> = _initDataAndNextScreen


    fun startInitAndNextScreen() {
        viewModelScope.launch {
            if(context.getCurrentLanguageCode().isBlank()) {
                /*TODO load dữ liệu lần đầu vào app*/
            }
            delay(1000L)
            _initDataAndNextScreen.postValue(true)
        }
    }

}