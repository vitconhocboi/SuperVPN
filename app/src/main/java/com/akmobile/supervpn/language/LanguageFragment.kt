package com.akmobile.supervpn.language

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.lingver.Lingver
import com.akmobile.supervpn.LanguageActivity
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentLanguageBinding
import com.akmobile.supervpn.R
import com.akmobile.supervpn.utils.Navigator
import java.util.Locale


class LanguageFragment : ProductFragment<FragmentLanguageBinding>() {
    private val mViewModel: LanguageViewModel by viewModels()
    private val mLangAdapter = LanguageAdapter()
    var languageCode = ""

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        mViewModel.init()
        binding.rcvLanguage.adapter = mLangAdapter
        val datas = ArrayList<LangDataModel>()
        for (lang in LangType.values()) {
            if (lang.langCode.isNotEmpty()) {
                datas.add(LangDataModel(getLanguageName(lang.langCode), lang))
            } else {
                datas.add(getSystemLanguage(lang))
            }
        }
        mLangAdapter.updateData(datas)

        binding.ivDone.setOnClickNoDoubleClick {
            BaseAppConfig.firstTimeSetup = false
            BaseAppConfig.languageCode = languageCode
            if (BaseAppConfig.languageCode.isNotEmpty()) {
                Lingver.getInstance().setLocale(requireContext(), BaseAppConfig.languageCode)
            } else {
                Lingver.getInstance().setFollowSystemLocale(requireContext())
            }

            (activity as? LanguageActivity)?.apply {
                Navigator.startMainActivity(this, null)
                finish()
            }
        }
    }

    private fun getSystemLanguage(lang: LangType): LangDataModel {
        val local = try {
            Resources.getSystem().configuration.locales[0]
        } catch (ex: Exception) {
            Resources.getSystem().configuration.locale
        }

        val isDuplicate = LangType.values()
            .count { local.language.contains(it.langCode) && it.langCode.isNotEmpty() } > 0

        val langName: String = requireActivity().getString(R.string.lang_default)

        val otherName: String = if (!isDuplicate) {
            val tmp = local.getDisplayLanguage(local)
            tmp.replaceFirstChar { it.uppercase() }
        } else {
            ""
        }
        return LangDataModel(langName, lang).apply { this.otherName = otherName }
    }


    fun getLanguageName(languageCode: String): String {
        val local = Locale(languageCode)
        val name = local.getDisplayName(local)
        return name.replaceFirstChar { it.uppercase() }
    }
}