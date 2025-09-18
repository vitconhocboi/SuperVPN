package com.core.baseui.customviews.keyboard

interface KeyboardProvider {
    fun onResume()
    fun onPause()
    fun addKeyboardListener(listener: KeyboardListener)
    fun removeKeyboardListener(listener: KeyboardListener)
    fun hideKeyboard()
}