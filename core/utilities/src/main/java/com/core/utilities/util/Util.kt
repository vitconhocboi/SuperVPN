package com.core.utilities.util

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun postDelay(timeMillisecond: Long, runnable: Runnable) {
    Handler(Looper.getMainLooper()).postDelayed(runnable, timeMillisecond)
}


fun Fragment.postDelayLifecycle(timeMillisecond: Long, action: () -> Unit) {
    val liveData = SingleLiveEvent<Unit>()
    Handler(Looper.getMainLooper()).postDelayed({
        liveData.postValue(Unit)
    }, timeMillisecond)
    liveData.observe(this) {
        action()
        liveData.removeObservers(this)
    }
}

fun AppCompatActivity.postDelayLifecycle(timeMillisecond: Long, action: () -> Unit) {
    val liveData = SingleLiveEvent<Unit>()
    Handler(Looper.getMainLooper()).postDelayed({
        liveData.postValue(Unit)
    }, timeMillisecond)
    liveData.observe(this) {
        action()
        liveData.removeObservers(this)
    }
}

fun postDelayLifecycle(timeMillisecond: Long, lifecycleOwner: LifecycleOwner, action: () -> Unit) {
    val liveData = SingleLiveEvent<Unit>()
    Handler(Looper.getMainLooper()).postDelayed({
        liveData.postValue(Unit)
    }, timeMillisecond)
    liveData.observe(lifecycleOwner) {
        action()
        liveData.removeObservers(lifecycleOwner)
    }
}
