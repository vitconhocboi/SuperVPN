package com.tici.vpn.proxy.master.settings.adsblock

import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.tici.vpn.proxy.master.databinding.AdRuleItemBinding
import javax.inject.Inject

/** Rule list rows: domain, per-rule switch, delete. Mirrors `AppProxyAdapter`. */
class AdRuleAdapter @Inject constructor() : BaseAdapter<AdRuleUI, BaseViewHolder<AdRuleUI>>() {

    /** Unfiltered list; [datas] holds what the current search shows. */
    private val listRecord: ArrayList<AdRuleUI> = arrayListOf()
    private var query = ""

    var onToggle: ((AdRuleUI) -> Unit)? = null
    var onDelete: ((AdRuleUI) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AdRuleUI> {
        return AdRuleViewHolder(
            AdRuleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    inner class AdRuleViewHolder(private val itemBinding: AdRuleItemBinding) :
        BaseViewHolder<AdRuleUI>(itemBinding.root) {
        override fun bindData(position: Int, data: AdRuleUI) {
            itemBinding.apply {
                tvDomain.text = data.displayName
                btnToggle.isSelected = data.enabled
                btnToggle.setOnClickListener {
                    data.enabled = !data.enabled
                    btnToggle.isSelected = data.enabled
                    onToggle?.invoke(data)
                }
                btnDelete.setOnClickListener { onDelete?.invoke(data) }
            }
        }
    }

    fun setData(list: List<AdRuleUI>) {
        listRecord.clear()
        listRecord.addAll(list)
        filter(query)
    }

    fun filter(text: String) {
        query = text.trim().lowercase()
        updateData(
            if (query.isEmpty()) listRecord
            else listRecord.filter {
                it.displayName.lowercase().contains(query) || it.domain.contains(query)
            }
        )
    }
}
