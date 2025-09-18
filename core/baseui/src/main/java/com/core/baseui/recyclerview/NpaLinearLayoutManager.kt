package com.core.baseui.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

class NpaLinearLayoutManager : LinearLayoutManager {

    override fun supportsPredictiveItemAnimations() = false

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context?, orientation: Int, reverseLayout: Boolean
    ) : super(context, orientation, reverseLayout)

    constructor(
        context: Context?
    ) : super(context)

}