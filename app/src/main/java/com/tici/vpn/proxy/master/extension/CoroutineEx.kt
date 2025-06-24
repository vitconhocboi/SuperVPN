package com.tici.vpn.proxy.master.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun <T>launchIO(doWork: suspend CoroutineScope.() -> T): Job {
    return CoroutineScope(Dispatchers.IO).launch {
        doWork.invoke(this)
    }
}

fun launchMain(doWork: () -> Unit): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        doWork.invoke()
    }
}