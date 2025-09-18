package com.core.utilities

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


// pass same viewgroup that was paased in showLoader(ViewGroup)
//@SuppressLint("ResourceType")
//fun Fragment.showLoader(rootView : ViewGroup, isDisableTouch: Boolean = true) {
//    val loaderAnimation = LottieAnimationView(requireActivity()).apply {
//        id = 12345
//        setAnimation(R.raw.loading)
//        repeatMode = LottieDrawable.INFINITE
//        loop(true)
//        layoutParams = ViewGroup.LayoutParams(50.toPx(), 50.toPx())
//        playAnimation()
//    }
//    loaderAnimation.doOnLayout {
//        it.x = rootView.width/2f - it.width/2
//        it.y = rootView.height/2f - it.height/2
//    }
//    if (isDisableTouch) {
//        disableTouch()
//    }
//    rootView.addView(loaderAnimation)
//}
//
//
//// pass same viewgroup that was paased in showLoader(ViewGroup)
//@SuppressLint("ResourceType")
//fun Fragment.removeLoader(rootView: ViewGroup){
//    val animationView = rootView.findViewById<LottieAnimationView>(12345)
//    rootView.removeView(animationView)
//    enableTouch()
//}


fun Fragment.enableTouch() {
    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Fragment.disableTouch() {
    requireActivity().window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Fragment.checkIfFragmentAttached(operation: Context.() -> Unit) {
    if (isAdded && context != null) {
        operation(requireContext())
    }
}

fun View.translate(from: Float, to: Float) {
    with(ValueAnimator.ofFloat(from, to)) {
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            translationY = animatedValue
        }
        start()
    }
}

fun View.animateScale(from: Int, to: Int, duration: Long = 1000) {
    val valueAnimator = ValueAnimator.ofInt(from, to)
    valueAnimator.duration = duration
    valueAnimator.addUpdateListener {
        val animatedValue = it.animatedValue as Int
        scaleX = animatedValue.toFloat()
        scaleY = animatedValue.toFloat()
    }
    valueAnimator.start()
}


fun EditText.afterTextChangedFlow(): Flow<String> {
    val query = MutableStateFlow("")
    doOnTextChanged { text, start, before, count ->
        query.value = text.toString()
    }
    return query
        .debounce(1000)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Main)
        .filter { query.value.isNotBlank() }
}

fun Fragment.launchIO(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        block.invoke(this)
    }
}


fun Fragment.launchDefault(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.Default) {
        block.invoke(this)
    }
}


fun Fragment.launchMain(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch(Dispatchers.Main) {
        block.invoke(this)
    }
}

fun Fragment.getAsDrawable(id: Int) = ContextCompat.getDrawable(this.requireActivity(), id)!!

fun Fragment.getAsColor(id: Int) = ContextCompat.getColor(this.requireActivity(), id)

fun Fragment.hideStatusBarUI() {
    WindowCompat.setDecorFitsSystemWindows(requireActivity().window,false)
    WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView).apply {
        // Hide the status bar
        hide(WindowInsetsCompat.Type.statusBars())
        // Allow showing the status bar with swiping from top to bottom
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Fragment.showStatusBarUI() {
    requireActivity().window?.decorView?.let { decorView ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window?.setDecorFitsSystemWindows(true)
            decorView.windowInsetsController?.show(WindowInsets.Type.systemBars())
        } else {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // layout Behind nav bar
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }
}


fun Fragment.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

fun Fragment.checkWindowReady(rootView: View, onReady: (displayCutout: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
        onReady(displayCutout())
        windowInsets
    }
}

fun Fragment.displayCutout(): Int {
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