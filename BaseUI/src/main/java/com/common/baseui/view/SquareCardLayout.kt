package com.common.baseui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.cardview.widget.CardView

class SquareCardLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    val RATIO_WIDTH_HEIGHT = 1f
    var ratio = RATIO_WIDTH_HEIGHT

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(
                (parentWidth * ratio).toInt(),
                MeasureSpec.EXACTLY
            )
        )
    }
}