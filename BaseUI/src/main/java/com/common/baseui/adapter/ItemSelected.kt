package com.common.baseui.adapter

import com.google.gson.annotations.SerializedName

abstract class ItemSelected (
    @SerializedName("selected")
    var selected: Boolean
) {
    abstract fun getIdentify(): String
}