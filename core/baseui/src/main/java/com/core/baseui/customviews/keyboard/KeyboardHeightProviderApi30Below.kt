package com.core.baseui.customviews.keyboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import com.core.baseui.R
import kotlin.math.abs


@SuppressLint("ClickableViewAccessibility")
class KeyboardHeightProviderApi30Below(private val activity: Activity) : PopupWindow(activity),
    KeyboardProvider {
    private var minHeight = 0
    private var resizableView: View
    private var parentView: View? = null
    private var lastKeyboardHeight = -1

    private var keyboardListeners = ArrayList<KeyboardListener>()

    init {
        contentView = View.inflate(activity, R.layout.keyboard_popup, null)
        resizableView = contentView.findViewById(R.id.keyResizeContainer)
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun onResume() {
        parentView = activity.findViewById(android.R.id.content)
        parentView?.post {
            resizableView.viewTreeObserver.addOnGlobalLayoutListener(getGlobalLayoutListener())
            if (!isShowing && parentView?.windowToken != null) {
                showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
            }
        }
    }

    override fun onPause() {
        resizableView.viewTreeObserver.removeOnGlobalLayoutListener(getGlobalLayoutListener())
        dismiss()
    }

    private fun getGlobalLayoutListener() = ViewTreeObserver.OnGlobalLayoutListener {
        computeKeyboardState()
    }

    private fun computeKeyboardState() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        resizableView.getWindowVisibleDisplayFrame(rect)
        val orientation = activity.resources.configuration.orientation

        val keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
        if (minHeight > keyboardHeight && keyboardHeight <= 0) {
            minHeight = keyboardHeight
        }
        KeyboardInfo.keyboardState = if (keyboardHeight > 0) KeyboardInfo.STATE_OPENED else KeyboardInfo.STATE_CLOSED
        if (keyboardHeight > 0) {
            KeyboardInfo.keyboardHeight = keyboardHeight
        }
        if (keyboardHeight != lastKeyboardHeight)
            notifyKeyboardHeightChanged(keyboardHeight + abs(minHeight), orientation)
        lastKeyboardHeight = keyboardHeight
    }

    private val topCutoutHeight: Int
        get() {
            val decorView = activity.window.decorView ?: return 0
            var cutOffHeight = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val windowInsets = decorView.rootWindowInsets
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val displayCutout = windowInsets.displayCutout
                    if (displayCutout != null) {
                        val list = displayCutout.boundingRects
                        for (rect in list) {
                            if (rect.top == 0) {
                                cutOffHeight += rect.bottom - rect.top
                            }
                        }
                    }
                }

            }
            return cutOffHeight
        }

    override fun addKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.add(listener)
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
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(parentView?.windowToken, 0)
    }

}