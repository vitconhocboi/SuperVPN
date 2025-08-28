package com.tici.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.tici.vpn.proxy.master.databinding.AdapterSkuItemBinding
import javax.inject.Inject

class SkuAdapter @Inject constructor() :
    BaseAdapter<Sku, BaseViewHolder<Sku>>() {
    var selected : Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<Sku> {
        return SkuViewHolder(
            AdapterSkuItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    inner class SkuViewHolder(private val mViewBinding: AdapterSkuItemBinding) :
        BaseViewHolder<Sku>(mViewBinding.root) {
        @SuppressLint("DiscouragedApi")
        override fun bindData(position: Int, data: Sku) {
            mViewBinding.apply {
                val resId = context.resources.getIdentifier(data.productName, "string", context.packageName)
                try {
                    skuName.text = context.getString(resId)
                } catch (e : Exception) {
                    skuName.text = "unknown"
                }
                skuPrice.text = data.productPrice
                root.isSelected = position == selected
//                checkboxSku.isChecked = position == selected
//                root.setOnClickListener {
//                    selected = position
//                    checkboxSku.isChecked = position == selected
//                    root.isSelected = position == selected
//                    onClickItem(data)
//                }

                sku.setOnClickListener {
                    selected = position
                    root.isSelected = position == selected
                    onClickItem(data)
                }
//                root.setOnClickListener {
//                    onClickItem(data)
//                }
            }
        }

        fun onClickItem(data: Sku) {
            onItemClick?.invoke(position, data)
        }
    }
}