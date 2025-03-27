package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.AdapterProxyGroupBinding
import com.akmobile.supervpn.home.ProxyGroupUI
import com.akmobile.supervpn.home.ProxyUI
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.setVisible
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

    var onProxyClick: ((position: Int, item: ProxyUI) -> Unit)? = null

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

                ivCollapse.setImageResource(if (data.collapsed) R.drawable.ic_arrow_down else R.drawable.ic_next_arrow)

                var mDailyAdapter = ProxyAdapter().apply {
                    updateData(data.list)
                }

                mDailyAdapter.onItemClick = { position, item ->
                    onProxyClick?.invoke(position, item)
                }
                rcvProxyItems.adapter = mDailyAdapter
                rcvProxyItems.setVisible(data.collapsed)
            }
        }

    }
}