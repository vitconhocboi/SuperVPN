package com.core.baseui.customviews.ratingBar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.core.baseui.R


open class BaseRatingBar : LinearLayout, SimpleRatingBar {

    private var mNumStars = 0
    private var mSpacing = 0
    private var mStarWidth = 0
    private var mStarHeight = 0
    private var mMinimumStars = 0f
    private var mRating = -1f
    private var mStepSize = 1f
    private var mPreviousRating = 0f

    private var mIsIndicator = false
    private var mIsScrollable = true
    private var mIsClickable = true
    private var mClearRatingEnabled = true

    private var mStartX = 0f
    private var mStartY = 0f

    private var mEmptyDrawable: Drawable? = null
    private var mFilledDrawable: Drawable? = null

    private var mOnRatingChangeListener: OnRatingChangeListener? = null

    protected var mPartialViews: ArrayList<PartialView>? = null

    constructor(context: Context?) : super(context, null){
        init(context,null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, 0){
        init(context,attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
       init(context,attrs)
    }

    private fun init(context: Context?,attrs: AttributeSet?){
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.BaseRatingBar)
        val rating: Float = typedArray?.getFloat(R.styleable.BaseRatingBar_srb_rating, 0F) ?: 0F
        initParamsValue(typedArray, context);
        verifyParamsValue()
        initRatingView()
        setRating(rating)
    }

    private fun initParamsValue(typedArray: TypedArray?, context: Context?) {
        mNumStars = typedArray?.getInt(R.styleable.BaseRatingBar_srb_numStars, mNumStars) ?: 0
        mStepSize = typedArray?.getFloat(R.styleable.BaseRatingBar_srb_stepSize, mStepSize) ?: 0F
        mMinimumStars = typedArray?.getFloat(R.styleable.BaseRatingBar_srb_minimumStars, mMinimumStars) ?: 0F
        mSpacing = typedArray?.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starSpacing, mSpacing) ?: 0
        mStarWidth = typedArray?.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starWidth, 0) ?: 0
        mStarHeight = typedArray?.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starHeight, 0) ?: 0
        mEmptyDrawable = if (typedArray?.hasValue(R.styleable.BaseRatingBar_srb_drawableEmpty) == true) {
            context?.let {
                ContextCompat.getDrawable(
                    it,
                    typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableEmpty, NO_ID)
                )
            }
        } else {
            null
        }
        mFilledDrawable = if (typedArray?.hasValue(R.styleable.BaseRatingBar_srb_drawableFilled) == true) {
            context?.let {
                ContextCompat.getDrawable(
                    it,
                    typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableFilled, NO_ID)
                )
            }
        } else {
            null
        }
        mIsIndicator = typedArray?.getBoolean(R.styleable.BaseRatingBar_srb_isIndicator, mIsIndicator) ?: false
        mIsScrollable = typedArray?.getBoolean(R.styleable.BaseRatingBar_srb_scrollable, mIsScrollable) ?: false
        mIsClickable = typedArray?.getBoolean(R.styleable.BaseRatingBar_srb_clickable, mIsClickable) ?: false
        mClearRatingEnabled = typedArray?.getBoolean(R.styleable.BaseRatingBar_srb_clearRatingEnabled, mClearRatingEnabled) ?: false
        typedArray?.recycle()
    }

    private fun verifyParamsValue() {
        if (mNumStars <= 0) {
            mNumStars = 5
        }
        if (mSpacing < 0) {
            mSpacing = 0
        }
        if (mEmptyDrawable == null) {
            mEmptyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star_rate_empty)
        }
        if (mFilledDrawable == null) {
            mFilledDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star_rate_full)
        }
        if (mStepSize > 1.0f) {
            mStepSize = 1.0f
        } else if (mStepSize < 0.1f) {
            mStepSize = 0.1f
        }
        mMinimumStars = RatingBarUtils.getValidMinimumStars(mMinimumStars, mNumStars, mStepSize)
    }

    private fun initRatingView() {
        mPartialViews = ArrayList()
        for (i in 1..mNumStars) {
            val partialView: PartialView = getPartialView(i, mStarWidth, mStarHeight, mSpacing, mFilledDrawable!!, mEmptyDrawable!!)
            addView(partialView)
            mPartialViews?.add(partialView)
        }
    }

    private fun getPartialView(
        partialViewId: Int, starWidth: Int, starHeight: Int, spacing: Int,
        filledDrawable: Drawable, emptyDrawable: Drawable,
    ): PartialView {
        val partialView = PartialView(context, partialViewId, starWidth, starHeight, spacing)
        partialView.setFilledDrawable(filledDrawable)
        partialView.setEmptyDrawable(emptyDrawable)
        return partialView
    }

    open fun emptyRatingBar() {
        fillRatingBar(0F)
    }

    protected open fun fillRatingBar(rating: Float) {
        for (partialView in mPartialViews!!) {
            val ratingViewId = partialView.tag as Int
            val maxIntOfRating = Math.ceil(rating.toDouble())
            if (ratingViewId > maxIntOfRating) {
                partialView.setEmpty()
                continue
            }
            if (ratingViewId.toDouble() == maxIntOfRating) {
                partialView.setPartialFilled(rating)
            } else {
                partialView.setFilled()
            }
        }
    }

    override fun setNumStars(numStars: Int) {
        if (numStars <= 0) {
            return
        }
        mPartialViews?.clear()
        removeAllViews()
        mNumStars = numStars
        initRatingView()
    }

    override fun getNumStars(): Int {
        return mNumStars
    }

    final override fun setRating(rating: Float) {
        setRating(rating, false);
    }

    open fun setRating(rating: Float, fromUser: Boolean) {
        var rating = rating
        if (rating > mNumStars) {
            rating = mNumStars.toFloat()
        }
        if (rating < mMinimumStars) {
            rating = mMinimumStars
        }
        if (mRating == rating) {
            return
        }

        // Respect Step size. So if the defined step size is 0.5, and we're attributing it a 4.7 rating,
        // it should actually be set to `4.5` rating.
        val stepAbidingRating = java.lang.Double.valueOf(Math.floor((rating / mStepSize).toDouble())).toFloat() * mStepSize
        mRating = stepAbidingRating
        mOnRatingChangeListener?.onRatingChange(this, mRating, fromUser)
        fillRatingBar(mRating)
    }

    override fun getRating(): Float {
        return mRating
    }

    // Unit is pixel
    override fun setStarWidth(@androidx.annotation.IntRange(from = 0) starWidth: Int) {
        mStarWidth = starWidth
        for (partialView in mPartialViews!!) {
            partialView.setStarWidth(starWidth)
        }
    }

    override fun getStarWidth(): Int {
        return mStarWidth
    }

    // Unit is pixel
    override fun setStarHeight(@IntRange(from = 0) starHeight: Int) {
        mStarHeight = starHeight
        for (partialView in mPartialViews!!) {
            partialView.setStarHeight(starHeight)
        }
    }

    override fun getStarHeight(): Int {
        return mStarHeight
    }

    override fun setStarPadding(ratingPadding: Int) {
        if (ratingPadding < 0) {
            return
        }
        mSpacing = ratingPadding
        for (partialView in mPartialViews!!) {
            partialView.setPadding(mSpacing, 0, mSpacing, 0)
        }
    }

    override fun getStarPadding(): Int {
        return mSpacing
    }

    override fun setEmptyDrawableRes(@DrawableRes res: Int) {
        val drawable = ContextCompat.getDrawable(context, res)
        drawable?.let { setEmptyDrawable(it) }
    }

    override fun setFilledDrawableRes(@DrawableRes res: Int) {
        val drawable = ContextCompat.getDrawable(context, res)
        drawable?.let { setFilledDrawable(it) }
    }

    override fun setEmptyDrawable(drawable: Drawable) {
        mEmptyDrawable = drawable
        for (partialView in mPartialViews!!) {
            partialView.setEmptyDrawable(drawable)
        }
    }

    override fun setFilledDrawable(drawable: Drawable) {
        mFilledDrawable = drawable
        for (partialView in mPartialViews!!) {
            partialView.setFilledDrawable(drawable)
        }
    }

    override fun setMinimumStars(@FloatRange(from = 0.0) minimumStars: Float) {
        mMinimumStars = RatingBarUtils.getValidMinimumStars(minimumStars, mNumStars, mStepSize)
    }

    override fun isIndicator(): Boolean {
        return mIsIndicator
    }

    override fun setIsIndicator(indicator: Boolean) {
        mIsIndicator = indicator
    }

    override fun isScrollable(): Boolean {
        return mIsScrollable
    }

    override fun setScrollable(scrollable: Boolean) {
        mIsScrollable = scrollable
    }

    override fun isClickable(): Boolean {
        return mIsClickable
    }

    override fun setClickable(clickable: Boolean) {
        mIsClickable = clickable
    }

    override fun setClearRatingEnabled(enabled: Boolean) {
        mClearRatingEnabled = enabled
    }

    override fun isClearRatingEnabled(): Boolean {
        return mClearRatingEnabled
    }

    override fun getStepSize(): Float {
        return mStepSize
    }

    override fun setStepSize(@FloatRange(from = 0.1, to = 1.0) stepSize: Float) {
        mStepSize = stepSize
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isIndicator()) {
            return false
        }
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = eventX
                mStartY = eventY
                mPreviousRating = mRating
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isScrollable()) {
                    return false
                }
                handleMoveEvent(eventX)
            }

            MotionEvent.ACTION_UP -> {
                if (!RatingBarUtils.isClickEvent(mStartX, mStartY, event) || !isClickable) {
                    return false
                }
                handleClickEvent(eventX)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    open fun handleMoveEvent(eventX: Float) {
        for (partialView in mPartialViews!!) {
            if (eventX < partialView.width / 10f + mMinimumStars * partialView.width) {
                setRating(mMinimumStars, true)
                return
            }
            if (!isPositionInRatingView(eventX, partialView)) {
                continue
            }
            val rating = RatingBarUtils.calculateRating(partialView, mStepSize, eventX)
            if (mRating != rating) {
                setRating(rating, true)
            }
        }
    }

    open fun handleClickEvent(eventX: Float) {
        for (partialView in mPartialViews!!) {
            if (!isPositionInRatingView(eventX, partialView)) {
                continue
            }
            val rating = if (mStepSize == 1f) (partialView.tag as Int).toFloat() else RatingBarUtils.calculateRating(partialView, mStepSize, eventX)
            if (mPreviousRating == rating && isClearRatingEnabled()) {
                setRating(mMinimumStars, true)
            } else {
                setRating(rating, true)
            }
            break
        }
    }

    open fun isPositionInRatingView(eventX: Float, ratingView: View): Boolean {
        return eventX > ratingView.left && eventX < ratingView.right
    }

    open fun setOnRatingChangeListener(onRatingChangeListener: OnRatingChangeListener) {
        mOnRatingChangeListener = onRatingChangeListener
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.setRating(mRating)
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setRating(ss.getRating())
    }

    interface OnRatingChangeListener {
        fun onRatingChange(ratingBar: BaseRatingBar?, rating: Float, fromUser: Boolean)
    }

}