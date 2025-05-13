package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.AdapterProxyGroupBinding
import com.akmobile.supervpn.utils.Utils
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.extension.setVisible
import javax.inject.Inject

class ProxyGroupAdapter @Inject constructor() : BaseAdapter<ProxyGroupUI, BaseViewHolder<ProxyGroupUI>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<ProxyGroupUI> {
        return ProxyGroupViewHolder(
            AdapterProxyGroupBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

//    var onProxyClick: ((position: Int, item: ProxyUI) -> Unit)? = null

    inner class ProxyGroupViewHolder(private val mViewBinding: AdapterProxyGroupBinding) :
        BaseViewHolder<ProxyGroupUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: ProxyGroupUI) {
            mViewBinding.apply {
                ivFlag.setImageResource(Utils.getFlag(data.country))
                tvTitle.setText(
                    context.getString(
                        context.resources.getIdentifier(
                            data.country, "string", context.packageName
                        )
                    )
                )

//                tvProxyIp.text = data.host

                lnMain.isSelected = data.active

                lnMain.setOnClickNoDoubleClick {
                    data.active = !data.active
                    notifyItemChanged(position)
                    onItemClick?.invoke(position, data)
                }


//                ivCollapse.setImageResource(if (data.collapsed) R.drawable.ic_arrow_down else R.drawable.ic_next_arrow)

//                var mDailyAdapter = ProxyAdapter().apply {
//                    updateData(data.list)
//                }

//                mDailyAdapter.onItemClick = { position, item ->
//                    onProxyClick?.invoke(position, item)
//                }
//                rcvProxyItems.adapter = mDailyAdapter
//                rcvProxyItems.setVisible(data.collapsed)
//
//                pnGroupProxy.setOnClickNoDoubleClick {
//                    data.collapsed = !data.collapsed
//                    ivCollapse.setImageResource(if (data.collapsed) R.drawable.ic_arrow_down else R.drawable.ic_next_arrow)
//                    rcvProxyItems.setVisible(data.collapsed)
//                }
            }
        }
    }
}