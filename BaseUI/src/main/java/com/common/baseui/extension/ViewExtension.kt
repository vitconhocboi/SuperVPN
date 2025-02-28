package com.common.baseui.extension

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd

fun View.setVisible(isVisible: Boolean, hideState: Int = View.GONE) {
    visibility = if(isVisible) {
        View.VISIBLE
    } else {
        hideState
    }
}

fun View.gone() {
    visibility = View.GONE
}
fun View.invisible() {
    visibility = View.GONE
}
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.setSize(width: Int ?= null, height: Int ?= null) {
    val layoutParams = this.layoutParams
    if(width != null) {
        layoutParams.width = width
    }

    if(height != null) {
        layoutParams.height = height
    }
    this.layoutParams = layoutParams

}


fun View.setOnClickNoDoubleClick(onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnClickCheckDoubleClick() {
        override fun onClickNoDoubleClick(view: View) {
            onClick.invoke(view)
        }
    })
}

fun View.setOnClickAndEffect(onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnClickCheckDoubleClick() {
        override fun onClickNoDoubleClick(view: View) {
            animate()
                .scaleX(1.1f) // Scale to 50% of the original size
                .scaleY(1.1f)
                .setDuration(200) // Duration for zoom-out animation
                .withEndAction {
                    // Reverse the zoom (zoom in) after the zoom out completes
                    animate()
                        .scaleX(1f) // Back to original size
                        .scaleY(1f)
                        .setDuration(200) // Duration for zoom-in animation
                        .start()
                }
                .start()
            onClick.invoke(view)
        }
    })
}

fun View.setOnClickNoDoubleClick(timeDelay: Long = 500, onClick: (View) -> Unit) {
    this.setOnClickListener(object : OnClickCheckDoubleClick(timeDelay = timeDelay) {
        override fun onClickNoDoubleClick(view: View) {
            onClick.invoke(view)
        }
    })
}


fun View.setOnClickNoDoubleClick(onClick: View.OnClickListener) {
    this.setOnClickListener(object : OnClickCheckDoubleClick() {
        override fun onClickNoDoubleClick(view: View) {
            onClick.onClick(view)
        }
    })
}

fun View.changeSizeWithAnimation(
    width: Int = -1,
    height: Int = -1,
    onUpdate: ((width: Int, height: Int) -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    duration: Long = 10L,
) {
    val valueAnimator = ValueAnimator()
    val propertyWidthName = "width"
    val propertyHeightName = "height"
    valueAnimator.setValues(
        PropertyValuesHolder.ofInt(propertyWidthName, this.measuredWidth, width),
        PropertyValuesHolder.ofInt(propertyHeightName, this.measuredHeight, height)
    )
    valueAnimator.duration = duration
    valueAnimator.addUpdateListener {
        val layoutParams = this.layoutParams
        val newWidth = valueAnimator.getAnimatedValue(propertyWidthName) as Int
        if (width >= 0) {
            layoutParams.width = newWidth
        }
        val newHeight = valueAnimator.getAnimatedValue(propertyHeightName) as Int
        if (height >= 0) {
            layoutParams.height = newHeight
        }
        this.layoutParams = layoutParams

        onUpdate?.invoke(newWidth, newHeight)
    }
    valueAnimator.doOnEnd { animator ->
        onDone?.invoke()
    }
    valueAnimator.start()
}