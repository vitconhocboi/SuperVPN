package com.core.utilities

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.core.utilities.component.OnSingleClick
import com.core.utilities.util.toast.Toasty

/**
 * View's visibility extensions.
 */

fun View.visible() {
    changeViewVisibility(View.VISIBLE)
}

fun View.gone() {
    changeViewVisibility(View.GONE)
}

fun View.invisible() {
    changeViewVisibility(View.INVISIBLE)
}

private fun View.changeViewVisibility(newState: Int) {
    visibility = newState
}

infix fun View.visibleIf(condition: Boolean) =
    run { visibility = if (condition) View.VISIBLE else View.GONE }

infix fun View.goneIf(condition: Boolean) =
    run { visibility = if (condition) View.GONE else View.VISIBLE }

infix fun View.invisibleIf(condition: Boolean) =
    run { visibility = if (condition) View.INVISIBLE else View.VISIBLE }


fun Fragment.toast(message: String, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(requireContext(), message, type, duration)
}

fun Fragment.toast(@StringRes message: Int, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(requireContext(), message, type, duration)
}

fun Activity.toast(message: String, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(this, message, type, duration)
}

fun Activity.toast(@StringRes message: Int, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(this, message, type, duration)
}

fun Context.toast(message: String, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(this, message, type, duration)
}

fun Context.toast(@StringRes message: Int, type: Int = Toasty.NORMAL, duration: Int = Toast.LENGTH_SHORT) {
    Toasty.showToast(this, message, type, duration)
}

fun View.showWithAnimationSlideUp() {
    if (this.isVisible) return
    visible()
    val animate = TranslateAnimation(
        0f,
        0f,
        this.height.toFloat(),
        0f
    )
    animate.duration = 200
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.goneWithAnimationSlideDown() {
    if (this.isGone) return
    gone()
    val animate = TranslateAnimation(
        0f,
        0f,
        0f,
        this.height.toFloat(),
    )
    animate.duration = 200
    animate.fillAfter = true
    this.startAnimation(animate)

}

/**
 * fades out a view making in completely invisible by default
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
 * @param endAlpha the alpha value that view will have after the animation completes default value is 0
 */
fun View.fadeOut(fadeDuration:Long = 300,endAlpha:Float = 0f){
    ValueAnimator.ofFloat(1f,endAlpha).apply {
        duration = fadeDuration
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
        }
    }.start()
}

/**
 * fades in a view
 * @param fadeDuration the duration of fade effect in milliseconds default value is 300ms
 */
fun View.fadeIn(fadeDuration:Long = 300){
    ValueAnimator.ofFloat(0f,1f).apply {
        duration = fadeDuration
        addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
        }
    }.start()
}

private var lastClickTime = 0L
private const val THRESHOLD_DOUBLE_TIME = 500

fun View.setOnClickPreventingDouble(onClick: () -> Unit) {
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < THRESHOLD_DOUBLE_TIME) {
            return@setOnClickListener
        }
        onClick.invoke()
        lastClickTime = SystemClock.elapsedRealtime()
    }
}

fun View.setOnClickPreventingSelfDouble(interval: Int = THRESHOLD_DOUBLE_TIME, onClick: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
            return@setOnClickListener
        }
        onClick.invoke()
        lastClickTime = SystemClock.elapsedRealtime()
    }
}

//fun View.setOnClickPreventingDouble(onClick: () -> Unit) {
//    var lastClickTime = 0L
//    val interval = 500
//
//    setOnClickListener {
//        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
//            return@setOnClickListener
//        }
//        onClick.invoke()
//        lastClickTime = SystemClock.elapsedRealtime()
//    }
//}


fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

fun View.margin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.let { leftMargin = it }
        top?.let { topMargin = it }
        right?.let { rightMargin = it }
        bottom?.let { bottomMargin = it }
    }
}

fun View.padding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(
        left ?: paddingLeft,
        top ?: paddingTop,
        right ?: paddingRight,
        bottom ?: paddingBottom,
    )
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun View.doOnDoubleClick(onDoubleClick: (View) -> Unit) {
    val safeClickListener = DoubleClickListener {
        onDoubleClick(it)
    }
    setOnClickListener(safeClickListener)
}

class DoubleClickListener(
    private var defaultInterval: Int = 300,
    private val onDoubleClick: (View) -> Unit
) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < defaultInterval) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }
}
 fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}
 fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

public inline fun SeekBar.setOnSeekBarChangeListener(
    crossinline progressBarChanged:
        (seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit =
        { _, _, _ ->
        },
    crossinline onStartTrackingTouch: (seekBar: SeekBar) -> Unit =
        { _ ->
        },
    crossinline onStopTrackingTouch: (seekBar: SeekBar) -> Unit = {}
): SeekBar.OnSeekBarChangeListener {
    val seekBarChangeListener = object: SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            progressBarChanged(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            onStopTrackingTouch(seekBar)
        }
    }

    setOnSeekBarChangeListener(seekBarChangeListener)
    return seekBarChangeListener
}

fun View.setOnSingleClick(onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnSingleClick() {
        override fun onSingleClick(view: View) {
            onClick.invoke(view)
        }
    })
}

fun View.setOnSingleClick(timeDelay: Long = 500, onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnSingleClick(timeDelay = timeDelay) {
        override fun onSingleClick(view: View) {
            onClick.invoke(view)
        }
    })
}


fun View.setOnSingleClick(onClick: View.OnClickListener) {
    this.setOnClickListener(object : OnSingleClick() {
        override fun onSingleClick(view: View) {
            onClick.onClick(view)
        }
    })
}

fun View.paddingTop(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, paddingBottom)
}

fun View.unfocus() {
    isFocusableInTouchMode = false
    isFocusable = false
    isFocusableInTouchMode = true
    isFocusable = true
}

fun View.changeSize(
    width: Int?= null,
    height: Int ?= null
) {
    val lp = this.layoutParams
    width?.let {
        lp.width = width
    }
    height?.let {
        lp.height = height
    }

    if(width != null || height != null) {
        this.layoutParams = lp
    }
}