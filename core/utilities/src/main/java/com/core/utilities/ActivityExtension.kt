package com.core.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.layout.WindowMetricsCalculator
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.core.utilities.util.toast.Toasty
import java.io.File

@SuppressLint("ResourceType")
fun Activity.showLoader(isDisableTouch: Boolean = true) {

    val loaderAnimation = LottieAnimationView(this).apply {
        setAnimation(R.raw.loading)
        repeatMode = LottieDrawable.INFINITE
        loop(true)
        playAnimation()
    }

    val frameLayout = FrameLayout(this).apply {
        id = 12345
        setBackgroundColor(ContextCompat.getColor(this@showLoader, R.color.background_dialog_dim))
        val size = resources.getDimensionPixelSize(com.core.dimens.R.dimen._160dp)
        val params = FrameLayout.LayoutParams(size, size)
        params.gravity = Gravity.CENTER
        addView(loaderAnimation, params)
    }

    val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content) as FrameLayout
    if (isDisableTouch) {
        disableTouch()
    }
    val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    rootView.addView(frameLayout, params)
}

@SuppressLint("ResourceType")
fun Activity.removeLoader() {
    val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
    val animationView = rootView.findViewById<View>(12345)
    rootView.removeView(animationView)
    enableTouch()
}

@SuppressLint("ResourceType")
fun Activity.isLoaderShowing(): Boolean {
    val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
    return rootView.findViewById<View>(12345) != null
}


@SuppressLint("ResourceType")
fun Activity.showDimForReopenApp(isDisableTouch: Boolean = true) {

    val frameLayout = FrameLayout(this).apply {
        id = 9999
        setBackgroundColor(ContextCompat.getColor(this@showDimForReopenApp, android.R.color.white))
        val size = resources.getDimensionPixelSize(com.core.dimens.R.dimen._160dp)
        val params = FrameLayout.LayoutParams(size, size)
        params.gravity = Gravity.CENTER
    }

    val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content) as FrameLayout
    if (isDisableTouch) {
        disableTouch()
    }
    val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    rootView.addView(frameLayout, params)
}

@SuppressLint("ResourceType")
fun Activity.removeDimForReopenApp() {
    val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
    val animationView = rootView.findViewById<View>(9999)
    rootView.removeView(animationView)
    enableTouch()
}

fun Activity.enableTouch() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Activity.disableTouch() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Activity.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

fun Activity.checkWindowReady( isHideStatusBar: Boolean, onReady: (displayCutout: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
        onReady(displayCutout(isHideStatusBar))
        windowInsets
    }
}

fun Activity.displayCutout(isHideStatusBar: Boolean): Int {
    val window = window
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
    return height.takeIf { it > 0 } ?: (if(isHideStatusBar) 0 else getStatusBarHeight())
}


fun Activity.hideNavigationBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView.findViewById(android.R.id.content)
    ).let { controller ->
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        // When the screen is swiped up at the bottom
        // of the application, the navigationBar shall
        // appear for some time
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Activity.showNavigationBar() {
    WindowInsetsControllerCompat(
        window,
        window.decorView.findViewById(android.R.id.content)
    ).let { controller ->
        controller.show(WindowInsetsCompat.Type.navigationBars())

        // When the screen is swiped up at the bottom
        // of the application, the navigationBar shall
        // appear for some time
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}


fun Activity.hideStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insetsController = window.insetsController ?: return
        insetsController.hide(WindowInsets.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        val insetsController = WindowInsetsControllerCompat(
            window,
            window.decorView
        )
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Activity.getAvailableScreenHeight(): Int {
    // Lấy đối tượng WindowMetricsCalculator
    val windowMetricsCalculator = WindowMetricsCalculator.getOrCreate()

    // Tính toán WindowMetrics hiện tại cho activity
    val metrics = windowMetricsCalculator.computeCurrentWindowMetrics(this)

    // Trả về chiều cao của vùng mà ứng dụng có thể vẽ lên
    return metrics.bounds.height()
}

fun Activity.fullScreen() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.hide(WindowInsets.Type.statusBars())
        window.insetsController?.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}

fun Activity.openFile(url: File, noAppCanOpenThisFileMessage: String) {
    try {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", url)
        val i = Intent(Intent.ACTION_VIEW)
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        i.setDataAndType(uri, contentResolver.getType(uri))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(i)
    } catch (e: Exception) {
        toast(noAppCanOpenThisFileMessage, type = Toasty.ERROR)
    }
}

fun Activity.getScreenWidth(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics

        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val view = window.decorView
        val rootWindowInsets = view.rootWindowInsets
        return if (rootWindowInsets != null) {
            val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets, view).getInsets(
                WindowInsetsCompat.Type.systemBars())
            displayMetrics.widthPixels - insets.left - insets.right
        } else {
            displayMetrics.widthPixels
        }
    }
}

fun Activity.getScreenHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics

        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

        windowMetrics.bounds.height() - insets.bottom - insets.top
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val view = window.decorView
        val rootWindowInsets = view.rootWindowInsets
        return if (rootWindowInsets != null) {
            val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets, view).getInsets(
                WindowInsetsCompat.Type.systemBars())

            displayMetrics.heightPixels - insets.bottom - insets.top
        } else {
            displayMetrics.heightPixels
        }

    }
}

fun Activity.getBannerAdWidth(): Int {
    val adWidthPixels = getScreenWidth()

    val density = resources.displayMetrics.density
    return (adWidthPixels / density).toInt()
}

fun Activity.checkIfActivityAlive(operation: Activity.() -> Unit) {
    if (!isDestroyed && !isFinishing) {
        operation(this)
    }
}
