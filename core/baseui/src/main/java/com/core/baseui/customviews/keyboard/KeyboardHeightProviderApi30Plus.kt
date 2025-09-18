package com.core.baseui.customviews.keyboard

import android.R
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class KeyboardHeightProviderApi30Plus(private val activity: Activity): KeyboardProvider {

    private var parentView: View? = null
    private var lastKeyboardHeight = -1
    private val handler = Handler(Looper.getMainLooper())
    private val keyboardListeners = ArrayList<KeyboardListener>()

    private var insetsListenerAttached = false

    override fun onResume() {
        parentView = activity.findViewById(R.id.content)
        parentView?.let { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: use WindowInsets
                if (!insetsListenerAttached) {
                    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                        val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                        // Trừ navigation bar nếu bị cộng vào IME
                        var keyboardHeight = imeInsets.bottom
                       /* if (keyboardHeight > 0 && navBarInsets.bottom > 0) {
                            keyboardHeight -= navBarInsets.bottom
                        }*/
                        keyboardHeight = keyboardHeight.coerceAtLeast(0)

                        val orientation = activity.resources.configuration.orientation

                        val isKeyboardVisible = keyboardHeight > 0

                        KeyboardInfo.keyboardState = if (isKeyboardVisible) {
                            KeyboardInfo.STATE_OPENED
                        } else {
                            KeyboardInfo.STATE_CLOSED
                        }

                        if (keyboardHeight != lastKeyboardHeight) {
                            notifyKeyboardHeightChanged(keyboardHeight, orientation)
                            lastKeyboardHeight = keyboardHeight
                        }

                        insets
                    }
                    insetsListenerAttached = true
                }
            } else {
                // API < 30: fallback
                view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            }
        }
    }

    override fun onPause() {
        parentView?.let { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setOnApplyWindowInsetsListener(view, null)
                insetsListenerAttached = false
            } else {
                view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            }
        }
    }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        parentView?.let { rootView ->
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height

            val keyboardHeight = screenHeight - rect.bottom
            val orientation = activity.resources.configuration.orientation

            val isKeyboardVisible = keyboardHeight > screenHeight * 0.15

            KeyboardInfo.keyboardState = if (isKeyboardVisible) {
                KeyboardInfo.STATE_OPENED
            } else {
                KeyboardInfo.STATE_CLOSED
            }

            if (keyboardHeight != lastKeyboardHeight) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    notifyKeyboardHeightChanged(
                        if (isKeyboardVisible) keyboardHeight else 0,
                        orientation
                    )
                }, 50)
                lastKeyboardHeight = keyboardHeight
            }
        }
    }

    override fun addKeyboardListener(listener: KeyboardListener) {
        if (!keyboardListeners.contains(listener)) {
            keyboardListeners.add(listener)
        }
    }

    override fun removeKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.remove(listener)
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardListeners.forEach {
            it.onHeightChanged(height)
        }
    }

    override fun hideKeyboard() {
        parentView?.let { view ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}
