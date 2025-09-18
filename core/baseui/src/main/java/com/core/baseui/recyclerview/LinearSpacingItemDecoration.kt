package com.core.baseui.recyclerview

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View

class LinearSpacingItemDecoration(
    private val verticalSpacing: Int,
    private val horizontalSpacing: Int = 0,
    private val bottomSpace: Int = 0,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        outRect.left = horizontalSpacing
        outRect.right = horizontalSpacing
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = verticalSpacing
        }
        if (itemPosition == itemCount - 1 && bottomSpace > 0) {
            outRect.bottom = bottomSpace
        } else {
            outRect.bottom = verticalSpacing
        }
    }
}