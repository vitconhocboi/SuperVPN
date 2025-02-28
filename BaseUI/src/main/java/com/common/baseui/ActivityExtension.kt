package com.common.baseui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

inline fun <T> AppCompatActivity.bind(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    source.onEach { action.invoke(it) }
        .launchIn(lifecycleScope)
}

inline fun <T> AppCompatActivity.bindFlow(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> AppCompatActivity.bindFlowResume(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> AppCompatActivity.bindFlowCreate(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> AppCompatActivity.bindLiveData(
    source: LiveData<T>,
    crossinline action: (T) -> Unit
) {
    source.observe(this) {
        action.invoke(it)
    }
}


inline fun <T> AppCompatActivity.processResultData(
    source: ResultData<T>,
    onSuccess: (T) -> Unit,
    onLoading: () -> Unit = {},
    onStandby: () -> Unit = {},
    onError: (Throwable?) -> Unit = {}
) {
    when(source.status) {
        ResultData.State.STANDBY -> {
            onStandby.invoke()
        }

        ResultData.State.LOADING -> {
            onLoading.invoke()
        }

        ResultData.State.SUCCESS -> {
            source.data?.let(onSuccess)
        }

        ResultData.State.ERROR -> {
            onError.invoke(source.errorException)
        }
    }
}