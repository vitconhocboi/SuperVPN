package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.databinding.AdapterProxyItemBinding
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.extension.setVisible
import javax.inject.Inject

class ProxyAdapter @Inject constructor() : BaseAdapter<ProxyUI, BaseViewHolder<ProxyUI>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<ProxyUI> {
        return ProxyViewHolder(
            AdapterProxyItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class ProxyViewHolder(private val mViewBinding: AdapterProxyItemBinding) :
        BaseViewHolder<ProxyUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: ProxyUI) {
            mViewBinding.apply {
                tvTitle.text = data.name
                tvProxyIp.text = data.host
                connectingState.setVisible(data.active)
                root.setOnClickNoDoubleClick {
                    onItemClick?.invoke(position, data)
                }
            }
        }
    }
}