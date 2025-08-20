package com.tici.vpn.proxy.master.guide

import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.tici.vpn.proxy.master.databinding.AdapterGuideBinding

class GuideAdapter : BaseAdapter<GuideModel, BaseViewHolder<GuideModel>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<GuideModel> {
        return GuideViewHolder(
            AdapterGuideBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class GuideViewHolder(private val mViewBinding: AdapterGuideBinding) :
        BaseViewHolder<GuideModel>(mViewBinding.root) {
        override fun bindData(position: Int, data: GuideModel) {
            mViewBinding.ivGuideImage.setImageResource(data.rawId)
            mViewBinding.tvDes.setText(data.resStringId)
        }
    }
}