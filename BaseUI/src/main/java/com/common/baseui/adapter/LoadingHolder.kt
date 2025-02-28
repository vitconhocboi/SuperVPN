package com.common.baseui.adapter

import android.view.View

open class LoadingHolder<T>(
    itemView: View
) : BaseViewHolder<T>(itemView) {

    override fun bindData(position: Int, data: T) {}

    open fun bind() {
        /*TODO*/
    }
}