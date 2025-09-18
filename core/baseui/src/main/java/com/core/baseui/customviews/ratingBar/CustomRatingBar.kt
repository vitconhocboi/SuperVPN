package com.core.baseui.customviews.ratingBar

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.core.baseui.R


class CustomRatingBar : AnimationRatingBar{

    companion object{
        const val ANIMATION_DELAY = 15L
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun emptyRatingBar() {
        // Need to remove all previous runnable to prevent emptyRatingBar and fillRatingBar out of sync
        if (mRunnable != null) {
            mHandler?.removeCallbacksAndMessages(mRunnableToken)
        }
        var delay: Long = 0
        for (view in mPartialViews!!) {
            mHandler?.postDelayed({ view.setEmpty() },5.let { delay += it; delay })
        }
    }

    override fun fillRatingBar(rating: Float) {
        // Need to remove all previous runnable to prevent emptyRatingBar and fillRatingBar out of sync
        if (mRunnable != null) {
            mHandler?.removeCallbacksAndMessages(mRunnableToken)
        }
        for (partialView in mPartialViews!!) {
            val ratingViewId = partialView.tag as Int
            val maxIntOfRating = Math.ceil(rating.toDouble())
            if (ratingViewId > maxIntOfRating) {
                partialView.setEmpty()
                continue
            }
            mRunnable = getAnimationRunnable(rating, partialView, ratingViewId, maxIntOfRating)
            postRunnable(mRunnable!!, ANIMATION_DELAY)
        }
    }

    private fun getAnimationRunnable(rating: Float, partialView: PartialView, ratingViewId: Int, maxIntOfRating: Double): Runnable {
        return Runnable {
            if (ratingViewId.toDouble() == maxIntOfRating) {
                partialView.setPartialFilled(rating)
            } else {
                partialView.setFilled()
            }
            if (ratingViewId.toFloat() == rating) {
                val scaleUp: Animation = AnimationUtils.loadAnimation(context, R.anim.rate_scale_up)
                val scaleDown: Animation = AnimationUtils.loadAnimation(context, R.anim.rate_scale_down)
                partialView.startAnimation(scaleUp)
                partialView.startAnimation(scaleDown)
            }
        }
    }

}