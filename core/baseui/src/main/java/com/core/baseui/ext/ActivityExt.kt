package com.core.baseui.ext

import android.os.SystemClock
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun FragmentActivity.popBackStack() = supportFragmentManager.popBackStack()

private const val THRESHOLD_FINISH_TIME = 2000
private var backPressedTime = 0L
val isBackPressFinish: Boolean
    get() {
        // preventing finish, using threshold of 2000 ms
        if (backPressedTime + THRESHOLD_FINISH_TIME > SystemClock.elapsedRealtime()) {
            return true
        }

        backPressedTime = SystemClock.elapsedRealtime()
        return false
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

fun LifecycleOwner.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block()
            this@launch.cancel()
        }
    }
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

fun AppCompatActivity.updateAlwaysOnDisplay(isEnable: Boolean) {
    if (isEnable) {
        window.addFlags(FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(FLAG_KEEP_SCREEN_ON)
    }
}

inline fun <T : Any?> AppCompatActivity.collectFlowOnNullable(
    stateFlow: StateFlow<T?>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T?) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any?> AppCompatActivity.collectFlowOnNullable(
    sharedFlow: SharedFlow<T?>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T?) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}


inline fun <T : Any> AppCompatActivity.collectFlowOn(
    stateFlow: StateFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any> AppCompatActivity.collectFlowOn(
    sharedFlow: SharedFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}


inline fun <T : Any?> collectFlowOnNullable(
    stateFlow: StateFlow<T?>,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T?) -> Unit,
) {
    lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}


inline fun <T : Any?> collectFlowOnNullable(
    sharedFlow: SharedFlow<T?>,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T?) -> Unit,
) {
    lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any> collectFlowOn(
    stateFlow: StateFlow<T>,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit,
) {
    lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any> collectFlowOn(
    sharedFlow: SharedFlow<T>,
    lifecycleScope: LifecycleCoroutineScope,
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit,
) {
    lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any> AppCompatActivity.collectFlowOn(
    flow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            flow.collect {
                onResult.invoke(it)
            }
        }
    }
}