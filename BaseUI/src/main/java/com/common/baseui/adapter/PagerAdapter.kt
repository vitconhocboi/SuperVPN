package com.common.baseui.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class PagerAdapter<T>(fragmentManager: FragmentManager, lifecycle: Lifecycle) :

FragmentStateAdapter(fragmentManager, lifecycle) {
    protected var data: ArrayList<T> = arrayListOf()

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateData(newData: List<T>?) {
        data.clear()
        data.addAll(newData ?: arrayListOf())
        notifyDataSetChanged()
    }

    fun addData(list: List<T>) {
        data.addAll(list)
        notifyDataSetChanged()
    }
}