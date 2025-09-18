package com.core.baseui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import com.core.baseui.R

class ScrollDividerNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var scrollOffsetY = 0
    private var showDividers = false
    private val paint = Paint().apply {
        color = context.getColor(R.color.neutral_light_elementary)
        strokeWidth = resources.getDimension(com.core.dimens.R.dimen._0_5dp)
    }

    init {
        setOnScrollChangeListener { _, _, scrollY, _, _ ->
            scrollOffsetY = scrollY
            val isAtTop = !canScrollVertically(-1)
            val isAtBottom = !canScrollVertically(1)
            showDividers = !isAtTop && !isAtBottom
            invalidate()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (showDividers) {
            // Draw the top divider
            canvas.drawLine(0f, scrollOffsetY.toFloat(), width.toFloat(), scrollOffsetY.toFloat(), paint)
            // Draw the bottom divider
            canvas.drawLine(0f, (scrollOffsetY + height).toFloat(), width.toFloat(), (scrollOffsetY + height).toFloat(), paint)
        }
    }
}