package com.common.baseui.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <T> Fragment.bindFlow(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindFlowResume(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindFlowCreate(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindLiveData(
    source: LiveData<T>,
    crossinline action: (T) -> Unit
) {
    source.observe(this) {
        action.invoke(it)
    }
}


inline fun <T> Fragment.processResultData(
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