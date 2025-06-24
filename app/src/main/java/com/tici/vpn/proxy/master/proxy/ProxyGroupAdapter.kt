package com.tici.vpn.proxy.master.proxy

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.BaseAppConfig
import com.tici.vpn.proxy.master.databinding.AdapterProxyGroupBinding
import com.tici.vpn.proxy.master.utils.Utils
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.common.baseui.extension.setOnClickNoDoubleClick
import javax.inject.Inject

class ProxyGroupAdapter @Inject constructor() :
    BaseAdapter<ProxyGroupUI, BaseViewHolder<ProxyGroupUI>>() {

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
    fun getStringSafely(context: Context, name: String): String? {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    inner class ProxyGroupViewHolder(private val mViewBinding: AdapterProxyGroupBinding) :
        BaseViewHolder<ProxyGroupUI>(mViewBinding.root) {
        override fun bindData(position: Int, data: ProxyGroupUI) {
            mViewBinding.apply {
                ivFlag.setImageResource(Utils.getFlag(data.country))
                tvTitle.setText(
                    getStringSafely(context = context, name = data.country) ?: "undefined"
                )

//                tvProxyIp.text = data.host

                lnMain.isSelected = data.active

                lnMain.setOnClickNoDoubleClick {
                    if (BaseAppConfig.isSub) {
                        focusToProxy(position)
                        notifyItemChanged(position)
                    }
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

    fun focusToProxy(position: Int) {
        datas.forEachIndexed { index, item ->
            if (position == index) {
                item?.active = true
                notifyItemChanged(index)
            } else if (item?.active == true) {
                item.active = false
                notifyItemChanged(index)
            }
        }
    }
}