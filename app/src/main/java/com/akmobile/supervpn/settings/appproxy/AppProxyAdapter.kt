package com.akmobile.supervpn.settings.appproxy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.databinding.AppProxyItemBinding
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import javax.inject.Inject

class AppProxyAdapter @Inject constructor() :
    BaseAdapter<AppProxyUI, BaseViewHolder<AppProxyUI>>() {
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
                root.setOnClickListener {
                    data.allowed = !data.allowed
                    checkboxProxy.isChecked = data.allowed
                    onItemClick?.invoke(position, data)
                }
            }
        }
    }
}