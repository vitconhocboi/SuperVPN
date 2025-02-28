package com.common.baseui.extension

import android.view.View
import timber.log.Timber

abstract class OnClickCheckDoubleClick(canClick: Boolean = true, var timeDelay: Long = 500L) : View.OnClickListener {
    private var canClick = true


    init {
        this.canClick = canClick
    }

    override fun onClick(view: View) {
        if (canClick) {
            Timber.d("canClick: $canClick")
            canClick = false
            view.isEnabled = false
            onClickNoDoubleClick(view)
            Utils.postDelay(timeDelay) {
                canClick = true
                try {
                    view.isEnabled = true
                }catch (ex: Exception) {
                    Timber.e(ex.message)
                }
                Timber.d("canClick2: $canClick")
            }
        }
    }

    abstract fun onClickNoDoubleClick(view: View)
}