package com.highsecure.vpn.proxy.master.settings.appproxy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import com.highsecure.vpn.proxy.master.databinding.AppProxyItemBinding
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import javax.inject.Inject

class AppProxyAdapter @Inject constructor() :
    BaseAdapter<AppProxyUI, BaseViewHolder<AppProxyUI>>() {

    var listRecord: ArrayList<AppProxyUI?> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<AppProxyUI> {
        return AppProxyViewHolder(
            AppProxyItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    inner class AppProxyViewHolder(private val mViewBinding: AppProxyItemBinding) :
        BaseViewHolder<AppProxyUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: AppProxyUI) {
            mViewBinding.apply {
                appName.text = data.appName
                appIcon.setImageDrawable(data.image)
                checkboxProxy.isChecked = data.allowed
                checkboxProxy.setOnClickListener {
                    onClickItem(data, checkboxProxy)
                }
                root.setOnClickListener {
                    onClickItem(data, checkboxProxy)
                }
            }
        }

        fun onClickItem(data: AppProxyUI, checkboxProxy: AppCompatCheckBox) {
            data.allowed = !data.allowed
            checkboxProxy.isChecked = data.allowed
            onItemClick?.invoke(position, data)
        }
    }

    fun setData(list: List<AppProxyUI?>) {
        listRecord.clear()
        listRecord.addAll(list)
    }

    fun filter(textChange: String) {
        val filteredList = listRecord.filter {
            it?.appName?.lowercase()?.contains(textChange.lowercase()) == true
        }
        updateData(filteredList)
    }
}