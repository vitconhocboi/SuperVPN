package com.core.utilities.component

import android.view.View
import com.core.utilities.util.postDelay

abstract class OnSingleClick(canClick: Boolean = true, var timeDelay: Long = 500L) : View.OnClickListener {
    private var canClick = true


    init {
        this.canClick = canClick
    }

    override fun onClick(view: View) {
        if (canClick) {
            canClick = false
            view.isEnabled = false
            onSingleClick(view)
            postDelay(timeDelay) {
                canClick = true
                try {
                    view.isEnabled = true
                }catch (ex: Exception) {
                }
            }
        }
    }

    abstract fun onSingleClick(view: View)
}