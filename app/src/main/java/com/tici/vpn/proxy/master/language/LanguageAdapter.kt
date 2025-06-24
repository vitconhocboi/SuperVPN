package com.tici.vpn.proxy.master.language

import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.baseui.adapter.BaseAdapter
import com.common.baseui.adapter.BaseViewHolder
import com.common.baseui.extension.context
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.ItemLanguageBinding

class LanguageAdapter : BaseAdapter<LangDataModel, BaseViewHolder<LangDataModel>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<LangDataModel> {
        return LangViewHolder(
            ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class LangViewHolder(val mViewBinding: ItemLanguageBinding) :
        BaseViewHolder<LangDataModel>(mViewBinding.root) {
        override fun bindData(position: Int, data: LangDataModel) {
            mViewBinding.apply {
                flagImageView.setImageResource(data.flagDrawableId)
                if (data.lang != LangType.DEFAULT) {
                    langNameLabel.text = data.langName
                } else {
                    langNameLabel.text = context.getString(R.string.lang_default)
                }

                lnMain.isSelected = data.isSelected

                lnMain.setOnClickNoDoubleClick {
                    focusToLang(data.langCode)
                    onItemClick?.invoke(position, data)
                }
                langNameLabel.setOnClickNoDoubleClick {
                    focusToLang(data.langCode)
                    onItemClick?.invoke(position, data)
                }
            }
        }
    }

    fun focusToLang(langCode: String) {
        datas.forEachIndexed { index, langDataModel ->
            if (langDataModel?.langCode == langCode) {
                langDataModel.isSelected = true
                notifyItemChanged(index)
            } else if (langDataModel?.isSelected == true) {
                langDataModel.isSelected = false
                notifyItemChanged(index)
            }
        }
    }
}