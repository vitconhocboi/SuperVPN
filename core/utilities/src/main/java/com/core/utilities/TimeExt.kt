package com.core.utilities

fun getCurrentTimeInSecond() = System.currentTimeMillis() / 1000

fun getCurrentTimeMillis() = System.currentTimeMillis()

fun Int.toMillis() = this * 1000L
fun Long.toMillis() = this * 1000L
