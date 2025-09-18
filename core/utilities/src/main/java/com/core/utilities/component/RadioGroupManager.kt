package com.core.utilities.component

import android.view.View
import com.core.utilities.setOnSingleClick
import com.core.utilities.util.Timber
import javax.inject.Inject

class RadioGroupManager @Inject constructor() {
    var radios: ArrayList<Radio> = arrayListOf()
    private var mOnChange: ((viewParent: View, tag: Any?) -> Unit) ?= null

    fun addRadio(radio: Radio) = apply { radios.apply { add(radio) } }

    fun selectedRadios(viewSelected: View, passChange: Boolean = false) {
        radios.forEach { radio ->
            val isContains = radio.views.contains(viewSelected) || radio.viewParent == viewSelected
            radio.views.forEach { view ->
                view.isSelected = isContains
            }
            if(isContains && passChange && radio.viewParent != null) {
                mOnChange?.invoke(radio.viewParent!!, radio.mTag)
            }
            radio.viewParent?.isSelected = isContains
        }
    }

    fun getViewSelected() = radios.firstOrNull { it.viewParent?.isSelected == true }

    fun selectedRadioByTag(tag: Any, passChange: Boolean = false) {
        radios.forEach { radio ->
            val isContains = radio.mTag == tag || radio.viewParent?.tag == tag
            radio.views.forEach { view ->
                view.isSelected = isContains
            }
            if(isContains && passChange && radio.viewParent != null) {
                mOnChange?.invoke(radio.viewParent!!, radio.mTag)
            }
            radio.viewParent?.isSelected = isContains
        }
    }

    fun clearSelected() {
        radios.forEach { radio ->
            radio.views.forEach { it.isSelected = false }
            radio.viewParent?.isSelected = false
        }
    }

    fun setRadioChange(delayClick: Long = 300, onChange: (viewParent: View, tag: Any?) -> Unit) {
        mOnChange = onChange
        radios.forEach { radio ->
            if (radio.viewParent == null) {
                Timber.e("View Parent is null")
            } else {
                radio.viewParent?.setOnSingleClick(timeDelay = delayClick) {
                    mOnChange?.invoke(it, radio.mTag)
                    selectedRadios(radio.viewParent!!)
                }
            }
        }
    }


    class Radio {
        var views: ArrayList<View> = arrayListOf()
        var viewParent: View ?= null
        var mTag: Any ?=null

        fun addView(view: View) = apply { views.apply { add(view) } }
        fun setTag(tag: Any) = apply { mTag = tag }
        fun addViewParent(view: View) = apply {
            viewParent = view
        }
    }
}

