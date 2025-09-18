package com.core.baseui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.core.baseui.BaseItemUI
import com.core.baseui.executor.AppExecutors

/**
 * A generic RecyclerView adapter that uses Data Binding & DiffUtil.
 *
 * @param <T> Type of the items in the list
 * @param <V> The type of the ViewDataBinding
</V></T> */
abstract class BaseMultiSelectListAdapter<T: BaseItemUI, V : BaseViewHolder<T>>(
    appExecutors: AppExecutors,
    diffCallback: DiffUtil.ItemCallback<T>
) : BaseListAdapter<T, BaseViewHolder<T>>(
    appExecutors, diffCallback
) {

    private var _listIdentifySelected : MutableSet<String> = mutableSetOf()

    var onMultiSelected: ((MutableSet<String>) -> Unit)? = null

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bindData(position, getItem(position))
    }

    fun toggleSelected(identify: String, autoNotify: Boolean = true, onToggle: (isSelected: Boolean) -> Unit = {}) {
        if(_listIdentifySelected.contains(identify)) {
            _listIdentifySelected.remove(identify)
            onToggle(false)
        } else {
            _listIdentifySelected.add(identify)
            onToggle(true)
        }
        if(autoNotify) {
            val index = currentList.indexOfFirst { it.identify == identify }
            if(index != -1) {
                notifyItemChanged(index)
            }
        }
        onMultiSelected?.invoke(_listIdentifySelected)
    }


    fun clearIdentifySelected() {
        _listIdentifySelected.clear()
    }

    fun getIdentifySelected() : MutableSet<String> {
        return _listIdentifySelected
    }

    fun isIdentifySelected(identify: String) : Boolean {
        return _listIdentifySelected.contains(identify)
    }

    fun isIdentifySelected(item: T) : Boolean {
        return _listIdentifySelected.contains(item.identify)
    }

    fun submitAndClearSelected(list: List<T>) {
        clearIdentifySelected()
        submitList(list)
    }
}
