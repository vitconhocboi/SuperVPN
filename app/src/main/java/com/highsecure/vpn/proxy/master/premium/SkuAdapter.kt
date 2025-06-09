package com.highsecure.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.highsecure.vpn.proxy.master.databinding.AdapterSkuItemBinding
import javax.inject.Inject

class SkuAdapter @Inject constructor() :
    BaseAdapter<Sku, BaseViewHolder<Sku>>() {

    var listRecord: ArrayList<Sku?> = arrayListOf()

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
                skuName.text = context.getString(resId)
                skuPrice.text = data.productPrice
                sku.setOnClickListener {
                    onClickItem(data)
                }
                root.setOnClickListener {
                    onClickItem(data)
                }
            }
        }

        fun onClickItem(data: Sku) {
            onItemClick?.invoke(position, data)
        }
    }

    fun setData(list: List<Sku?>) {
        listRecord.clear()
        listRecord.addAll(list)
    }
}