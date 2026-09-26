package com.tici.vpn.proxy.master.settings.dns

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.tici.vpn.proxy.master.databinding.AdapterDnsBinding
import javax.inject.Inject

class DnsAdapter @Inject constructor() : BaseAdapter<DnsUI, BaseViewHolder<DnsUI>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<DnsUI> {
        return ProxyGroupViewHolder(
            AdapterDnsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class ProxyGroupViewHolder(private val mViewBinding: AdapterDnsBinding) :
        BaseViewHolder<DnsUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: DnsUI) {
            mViewBinding.apply {
                ivFlag.visibility = View.GONE
                tvTitle.text = data.name
                tvProxyIp.text = data.server
                lnMain.isSelected = data.active
                ivSelect.isSelected = data.active

                lnMain.setOnClickNoDoubleClick {
                    val wasActive = data.active
                    datas.forEach { item ->
                        item?.active = false
                    }
                    data.active = !wasActive
                    notifyDataSetChanged()
                }
            }
        }
    }
}
