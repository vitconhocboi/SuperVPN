package com.core.baseui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.baseui.fragment.CoreScreenType
import com.core.baseui.fragment.ScreenType
import com.core.baseui.navigator.NavigatorEvent
import com.core.config.domain.RemoteConfigRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class BaseSharedViewModel<E : NavigatorEvent>(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val handle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val KEY_CURRENT_SCREEN = "KEY_CURRENT_SCREEN"
    }

    private val _navigateToFlow = MutableSharedFlow<E>()

    val navigateToFlow = _navigateToFlow.asSharedFlow()

    lateinit var currentEvent: E
    var needHandleEventWhenResume = false
    var isActivityResume = false

    var previousNetworkConnection = true

    abstract fun navigateActionBack()


    val isHideNavigationBar: Boolean by lazy {
        remoteConfigRepository.getAppConfig().isHideNavigationBar
    }

    fun navigateTo(event: E) {
        viewModelScope.launch {
            currentEvent = event
            _navigateToFlow.emit(event)
        }
    }

}