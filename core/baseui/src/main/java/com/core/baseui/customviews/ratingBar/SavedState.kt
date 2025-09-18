package com.core.baseui.customviews.ratingBar

import android.os.Parcel
import android.os.Parcelable
import android.view.View


class SavedState : View.BaseSavedState {
    private var rating = 0f

    constructor(superState: Parcelable?) : super(superState)

    constructor(parcel : Parcel) : super(parcel){
        rating = parcel.readFloat()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeFloat(rating)
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(source: Parcel): SavedState {
            return SavedState(source)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }

    fun getRating(): Float {
        return rating
    }

    fun setRating(rating: Float) {
        this.rating = rating
    }

}