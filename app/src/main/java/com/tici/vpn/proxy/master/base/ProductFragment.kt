package com.tici.vpn.proxy.master.base

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.tici.vpn.proxy.master.R
import com.common.baseui.BindingFragment
import com.common.baseui.toast.Toasty

abstract class ProductFragment<T : ViewBinding>: BindingFragment<T>() {
    fun showError() {
        Toasty.error(requireContext(), getString(R.string.text_error)).show()
    }

    override fun isBuyApp(): Boolean {
        return false
    }

    fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val window = activity?.window
        if(window != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsets = window.decorView.getRootWindowInsets()
                statusBarHeight = windowInsets?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0
            } else {
                val rectangle = Rect()
                window.decorView.getWindowVisibleDisplayFrame(rectangle)
                statusBarHeight = rectangle.top
            }
        }

        if(statusBarHeight == 0) {
            statusBarHeight = try {
                resources.getDimensionPixelSize(
                    resources.getIdentifier("status_bar_height", "dimen", "android")
                )
            } catch (ex: Exception) {
                resources.getDimensionPixelSize(com.common.baseui.R.dimen.size25)
            }

        }
        return statusBarHeight
    }

    fun checkWindowReady(rootView: View, onReady: () -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            onReady()
            windowInsets
        }
    }

    fun displayCutout(): Int {
        val window = activity?.window
        var height = 0
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val windowInsets = window.decorView.rootWindowInsets
                if (windowInsets != null) {
                    val displayCutout = windowInsets.displayCutout
                    if (displayCutout != null) {
                        height = displayCutout.safeInsetTop
                        // Use cutoutHeight as needed (e.g., adjust layout margins)
                    }
                }
            }
        }
        return height
    }
}