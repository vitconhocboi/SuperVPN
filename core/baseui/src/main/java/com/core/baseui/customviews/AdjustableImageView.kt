package com.core.baseui.customviews

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewParent
import androidx.appcompat.widget.AppCompatImageView

class AdjustableImageView : AppCompatImageView {

    private val isInScrollingContainer: Boolean
        get() {
            var p: ViewParent? = parent
            while (p != null && p is ViewGroup) {
                if (p.shouldDelayChildPressedState()) {
                    return true
                }
                p = p.parent
            }
            return false
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mDrawable = drawable
        if (mDrawable == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        if (adjustViewBounds) {
            val mDrawableWidth = mDrawable.intrinsicWidth
            val mDrawableHeight = mDrawable.intrinsicHeight
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)

            if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
                // Fixed Height & Adjustable Width
                val width = heightSize * mDrawableWidth / mDrawableHeight
                if (isInScrollingContainer)
                    setMeasuredDimension(width, heightSize)
                else
                    setMeasuredDimension(
                        width.coerceAtMost(widthSize),
                        heightSize.coerceAtMost(heightSize)
                    )
            } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                // Fixed Width & Adjustable Height
                val height = widthSize * mDrawableHeight / mDrawableWidth
                if (isInScrollingContainer)
                    setMeasuredDimension(widthSize, height)
                else
                    setMeasuredDimension(
                        widthSize.coerceAtMost(widthSize),
                        height.coerceAtMost(heightSize)
                    )
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}
