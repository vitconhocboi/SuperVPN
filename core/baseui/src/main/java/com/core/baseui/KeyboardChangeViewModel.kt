package com.core.baseui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class KeyboardChangeViewModel @Inject constructor() : ViewModel() {
    private val _keyboardHeight = MutableStateFlow<Int?>(null)
    val keyboardHeight: StateFlow<Int?> get() = _keyboardHeight


    fun updateKeyboardHeight(height: Int?) {
        _keyboardHeight.value = height
    }
}
