package com.akmobile.supervpn.proxy

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.AdapterProxyGroupBinding
import com.akmobile.supervpn.home.ProxyGroupUI
import com.akmobile.supervpn.network.dns.Resource
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import javax.inject.Inject

class ProxyGroupAdapter @Inject constructor() :
    BaseAdapter<ProxyGroupUI, BaseViewHolder<ProxyGroupUI>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ProxyGroupUI> {
        return ProxyGroupViewHolder(
            AdapterProxyGroupBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class ProxyGroupViewHolder(private val mViewBinding: AdapterProxyGroupBinding) :
        BaseViewHolder<ProxyGroupUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: ProxyGroupUI) {
            mViewBinding.apply {
                ivFlag.setImageResource(
                    when (data.country) {
                        "vietnam" -> R.drawable.ic_flag_vietnam
                        "us" -> R.drawable.flag_us
                        "korea" -> R.drawable.ic_flag_korea
                        "arab" -> R.drawable.ic_flag_arab
                        "india" -> R.drawable.ic_flag_india
                        "japan" -> R.drawable.ic_flag_japan
                        else -> R.drawable.ic_flag_default
                    }
                )
                tvTitle.setText(data.group)
            }
        }

    }
}