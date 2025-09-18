package com.core.ads.customviews.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.core.ads.R

class AspectRatioFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var ratio = 1f

    init {
        context.withStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout) {
            ratio = getFloat(R.styleable.AspectRatioFrameLayout_AspectRatioFrameLayout_ratio, 1f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((parentWidth * ratio).toInt(), MeasureSpec.EXACTLY))
    }
}