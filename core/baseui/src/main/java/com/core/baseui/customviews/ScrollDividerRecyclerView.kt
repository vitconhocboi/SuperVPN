package com.core.baseui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.core.baseui.R

class ScrollDividerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var showDividers = false
    private val paint = Paint().apply {
        color = context.getColor(R.color.neutral_light_elementary)
        strokeWidth = resources.getDimension(com.core.dimens.R.dimen._0_5dp)
    }

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isAtTop = !recyclerView.canScrollVertically(-1)
                val isAtBottom = !recyclerView.canScrollVertically(1)
                showDividers = !isAtTop && !isAtBottom
                invalidate()
            }

//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                showDividers = when (newState) {
//                    SCROLL_STATE_IDLE -> false
//                    SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING -> true
//                    else -> false
//                }
//                invalidate()
//            }
        })
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (showDividers) {
            // Draw the top divider
            canvas.drawLine(0f, 0f, width.toFloat(), 0f, paint)
            // Draw the bottom divider
            canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), paint)
        }
    }
}