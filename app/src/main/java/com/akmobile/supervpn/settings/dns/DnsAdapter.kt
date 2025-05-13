package com.akmobile.supervpn.settings.dns

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.AdapterDnsBinding
import com.akmobile.supervpn.databinding.AdapterProxyGroupBinding
import com.akmobile.supervpn.utils.Utils
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.extension.setVisible
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

//    var onProxyClick: ((position: Int, item: ProxyUI) -> Unit)? = null

    inner class ProxyGroupViewHolder(private val mViewBinding: AdapterDnsBinding) :
        BaseViewHolder<DnsUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: DnsUI) {
            mViewBinding.apply {
//                ivFlag.setImageResource(Utils.getFlag(data.country))
                tvTitle.text = data.name

                tvProxyIp.text = data.server

                lnMain.isSelected = data.active

                lnMain.setOnClickNoDoubleClick {
                    data.active = !data.active
                    notifyItemChanged(position)
                }
            }
        }
    }
}