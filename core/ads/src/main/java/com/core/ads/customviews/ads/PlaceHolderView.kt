package com.core.ads.customviews.ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout

class PlaceHolderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var shimmerLayout: ShimmerFrameLayout? = null

    fun setPlaceHolder(layoutResId: Int) {
        // Dùng parent = this, attachToRoot = false để LayoutInflater tạo đúng LayoutParams (có margin)
        val view = LayoutInflater.from(context).inflate(layoutResId, this, false)
        shimmerLayout = view as? ShimmerFrameLayout ?: throw RuntimeException("Layout must be a ShimmerFrameLayout")

        // Không tự tạo LayoutParams mới -> giữ nguyên margin từ XML
        removeAllViews()
        // Dùng chính layoutParams đã được inflate ra
        val lp = shimmerLayout?.layoutParams ?: LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(shimmerLayout, lp)
    }

    fun startShimmer() {
        shimmerLayout?.startShimmer()
    }

    fun stopShimmer() {
        shimmerLayout?.stopShimmer()
    }
}