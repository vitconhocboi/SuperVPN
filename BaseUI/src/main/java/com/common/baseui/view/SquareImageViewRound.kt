package com.common.baseui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.common.baseui.view.roundimage.RoundedImageView

open class SquareImageViewRound @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RoundedImageView(context, attrs, defStyleAttr) {
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