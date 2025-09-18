package com.core.utilities.util

import android.app.Dialog
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Dialog.hideNavigationBar() {
    WindowInsetsControllerCompat(
        window!!,
        window!!.decorView
    ).let { controller ->
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        // When the screen is swiped up at the bottom
        // of the application, the navigationBar shall
        // appear for some time
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

}

fun Dialog.hideNavigationBarForBottomDialog(onlyHideNavigationBar: Boolean = false) {
    window?.let { window ->
        // Ẩn navigation bar
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (onlyHideNavigationBar) return

        // Kéo layout fullscreen
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Chặn WindowInsets "chừa khoảng"
        window.decorView.setOnApplyWindowInsetsListener { v, insets ->
            // Trả về insets trống để không chừa padding
            v.onApplyWindowInsets(WindowInsets.CONSUMED)
            WindowInsets.CONSUMED
        }
    }
}