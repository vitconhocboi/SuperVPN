package com.core.baseui

import androidx.core.graphics.Insets
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InsetsViewModel @Inject constructor() : ViewModel() {
    private val _systemInsets = MutableStateFlow<WrapInsets?>(null)
    val systemInsets: StateFlow<WrapInsets?> get() = _systemInsets


    fun updateInsets(insets: WrapInsets) {
        _systemInsets.value = insets
    }

    class WrapInsets(
        val insets: Insets?,
        val hideStatusBar: Boolean,
        val isActivitySpaceStatusBar: Boolean,
    )
}
