package com.core.baseui.customviews

import android.content.Context
import android.util.AttributeSet
import com.core.baseui.R
import com.google.android.material.card.MaterialCardView


open class RectangleCardView : MaterialCardView {
    var heightPerWidth: Float = 1f

    constructor(context: Context) : super(context) {
        parseAttributes(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        parseAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        parseAttributes(context, attrs)
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RectangleCardView
        )
        try {
            heightPerWidth = a.getFloat(
                R.styleable.RectangleCardView_heightPerWidth, 1f
            )
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, (widthMeasureSpec * heightPerWidth).toInt())
    }

}

