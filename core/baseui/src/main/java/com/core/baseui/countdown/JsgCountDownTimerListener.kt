package com.core.baseui.countdown

interface JsgCountDownTimerListener {
    fun onTimerTick(timeRemaining: Long)

    fun onTimerFinish()
}