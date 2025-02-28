package com.simple.libads

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class FrameAds @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var isForceHide = false

    fun forceHide(isForce: Boolean) {
        isForceHide = isForce
        if(isForce) {
            gone()
        }
    }

    override fun addView(child: View?) {
        if(isForceHide) {
            return
        }
        super.addView(child)
    }

    override fun setVisibility(visibility: Int) {
        if(isForceHide) {
            super.setVisibility(View.GONE)
            return
        }
        super.setVisibility(visibility)
    }
}