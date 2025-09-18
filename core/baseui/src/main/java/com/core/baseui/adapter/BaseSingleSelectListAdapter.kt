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
abstract class BaseSingleSelectListAdapter<T : BaseItemUI, V : BaseViewHolder<T>>(
    appExecutors: AppExecutors,
    diffCallback: DiffUtil.ItemCallback<T>
) : BaseListAdapter<T, BaseViewHolder<T>>(
    appExecutors, diffCallback
) {

    private var _identifySelect: String? = null
    val identifySelect: String?
        get() = _identifySelect

    var onSingleSelected: ((position: Int, data: T) -> Unit)? = null

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bindData(position, getItem(position))
    }

    fun singleSelected(
        identify: String,
        onSingleSelected: ((position: Int, data: T) -> Unit)? = null
    ) {
        if (_identifySelect != identify) {
            val index = currentList.indexOfFirst { it.identify == _identifySelect }

            val indexSelected = currentList.indexOfFirst { it.identify == identify }

            _identifySelect = identify
            if (index != -1) {
                notifyItemChanged(index)
            }
            if (indexSelected != -1) {
                notifyItemChanged(indexSelected)
            }
            onSingleSelected?.invoke(indexSelected, getItem(indexSelected))
            this.onSingleSelected?.invoke(indexSelected, getItem(indexSelected))
        }
    }

    fun clearIdentifySelected() {
        _identifySelect = null
    }

    fun isIdentifySelected(identify: String) : Boolean {
        return identify == _identifySelect
    }

    fun isIdentifySelected(item: T) : Boolean {
        return item.identify == _identifySelect
    }

    fun submitAndClearSelected(list: List<T>) {
        clearIdentifySelected()
        submitList(list)
    }
}
