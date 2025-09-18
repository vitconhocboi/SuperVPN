package com.core.ads.customviews.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.core.ads.R
import com.facebook.shimmer.ShimmerFrameLayout

class AspectRatioShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShimmerFrameLayout(context, attrs, defStyleAttr) {
    var ratio = 1f

    init {
        context.withStyledAttributes(attrs, R.styleable.AspectRatioShimmerLayout) {
            ratio = getFloat(R.styleable.AspectRatioShimmerLayout_AspectRatioShimmerLayout_ratio, 1f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((parentWidth * ratio).toInt(), MeasureSpec.EXACTLY))
    }
}