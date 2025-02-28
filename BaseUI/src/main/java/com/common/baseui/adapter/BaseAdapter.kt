package com.common.baseui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.common.baseui.R

abstract class BaseAdapter<D, H : BaseViewHolder<D>> :
    RecyclerView.Adapter<H>() {
    var datas: ArrayList<D?> = arrayListOf()
    var recyclerView: RecyclerView? = null
    var onUpdateData: ((ArrayList<D?>) -> Unit)? = null
    var onAddData: ((ArrayList<D?>) -> Unit)? = null
    var onItemClick: ((position: Int, item: D) -> Unit)? = null
    var onAddItem: ((position: Int, item: D) -> Unit)? = null
    var onItemViewClick: ((view: View, position: Int, item: D) -> Unit)? = null
    var onSingleItemSelected: ((position: Int, item: D?, sameItem: Boolean) -> Unit)? = null
    var onSingleItemChecked: ((position: Int,checked: Boolean, item: D?) -> Unit)? = null
    var onMultiItemSelected: ((item: List<D>) -> Unit)? = null
    var onOptionClick: ((view: View, position: Int, item: D) -> Unit)? = null
    val TYPE_ITEM_LOADING = -101
    val TYPE_ITEM_NORMAL = -100
    val TYPE_ITEM_SECTION = -99
    val TYPE_ITEM_ADS = -98
    val TYPE_ITEM_ADD= -97

    var onLongClickItem: ((view: View, position: Int, item: D) -> Unit)? = null
    val TYPE_ITEM_HEADER = -97
    open fun addLoading() {
        if (!datas.contains(null)) {
            datas.add(null)
            notifyItemInserted(datas.size)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    open fun removeLoading() {
        if (datas.contains(null)) {
            datas.remove(null)
            notifyItemRemoved(itemCount)
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: H, position: Int) {
        if (holder is LoadingHolder<*>) {
            holder.bind()
        } else {
            datas.getOrNull(position)?.let {
                holder.bindData(position, it)
            }
        }
    }

    open fun updateData(newData: List<D?>) {
        val isLoading = datas.contains(null)
        this.datas.clear()
        this.datas.addAll(newData)
        if (isLoading) {
            this.datas.add(null)
        }
        onUpdateData?.invoke(datas)
        notifyDataSetChanged()
    }

    fun clearData() {
        datas.clear()
        notifyDataSetChanged()
    }

    open fun addDatas(newData: ArrayList<D?>) {
        val position = datas.size
        datas.addAll(newData)
        onAddData?.invoke(newData)
        notifyItemRangeInserted(position, datas.count())
    }


    open fun addData(data: D) {
        val position = datas.size
        datas.add(data)
        onAddData?.invoke(ArrayList<D?>().apply {
            add(data)
        })
        notifyItemRangeInserted(position, datas.count())
    }

    fun addDataFirst(data: D) {
        datas.add(0, data)
        onAddData?.invoke(ArrayList<D?>().apply {
            add(data)
        })
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (datas[position] == null) TYPE_ITEM_LOADING else TYPE_ITEM_NORMAL
    }

    open fun getLoadingViewHolder(
        parent: ViewGroup,
        @LayoutRes idResLayout: Int? = null,
    ): BaseViewHolder<D> {
        return createLoadingViewHolder(idResLayout, parent, parent.context)
    }

    private fun createLoadingViewHolder(
        @LayoutRes idResLayout: Int? = null,
        parent: ViewGroup,
        context: Context,
    ): LoadingHolder<D> {
        return LoadingHolder(
            LayoutInflater.from(context).inflate(
                idResLayout ?: R.layout.item_loading,
                parent,
                false
            )
        )
    }

    fun getItem(position: Int): D? {
        return datas.getOrNull(position)
    }

    fun selectedSingleItem(selected: D, isUnselectDuplicate: Boolean = false, onSame: (isSameItem: Boolean) -> Unit = {}): Int {
        val oldSelectedItem = datas.firstOrNull {
            it is ItemSelected && it.selected
        }
        val isSame = oldSelectedItem == selected
        (selected as? ItemSelected)?.let {
            if (isUnselectDuplicate) {
                it.selected = !it.selected
            } else {
                it.selected = true
            }
        }
        var indexItemSelected = -1
        datas.forEachIndexed { index, item ->
            if (item is ItemSelected) {
                if (selected != item) {
                    if (item.selected) {
                        item.selected = false
                        notifyItemChanged(index, Unit)
                    }
                } else {
                    notifyItemChanged(index, Unit)
                    indexItemSelected = index
                }
            }
        }
        onSame.invoke(isSame)
        return indexItemSelected
    }

    fun unselectedAllItem() {
        datas.forEachIndexed { index, item ->
            if (item is ItemSelected) {
                if (item.selected) {
                    item.selected = false
                    notifyItemChanged(index, Unit)
                }
            }
        }
    }

    fun selectedSingleItem(position: Int) {
        val selected = datas.getOrNull(position) ?: return
        selectedSingleItem(selected)
    }

    fun notifyItem(item: D) {
        if (item is ItemSelected) {
            val index = datas.indexOfFirst {
                (it as? ItemSelected)?.getIdentify() == item.getIdentify()
            }
            if (index != -1) {
                notifyItemChanged(index, Unit)
            }
        }
    }

    fun notifyItem(id: String) {
        val index = datas.indexOfFirst {
            (it as? ItemSelected)?.getIdentify() == id
        }

        if (index != -1) {
            notifyItemChanged(index, Unit)
        }
    }

    fun notifyItemSelected() {
        val index = datas.indexOfFirst {
            (it as? ItemSelected)?.selected == true
        }
        if (index != -1) {
            notifyItemChanged(index, Unit)
        }
    }


}