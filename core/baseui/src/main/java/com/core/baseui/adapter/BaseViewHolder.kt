package com.core.baseui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<T>(private val _binding: ViewBinding) : RecyclerView.ViewHolder(_binding.root) {

    abstract fun bindData(position: Int, data: T)
}
