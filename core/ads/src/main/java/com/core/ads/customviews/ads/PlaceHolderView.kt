package com.core.ads.customviews.ads

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout

class PlaceHolderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var shimmerLayout: ShimmerFrameLayout? = null

    fun setPlaceHolder(layoutResId: Int) {
        shimmerLayout = (inflate(context, layoutResId, null) as? ShimmerFrameLayout) ?: throw RuntimeException("Layout must be a ShimmerFrameLayout")
        removeAllViews()
        addView(shimmerLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun startShimmer() {
        shimmerLayout?.startShimmer()
    }

    fun stopShimmer() {
        shimmerLayout?.stopShimmer()
    }
}