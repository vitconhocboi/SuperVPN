package com.tici.vpn.proxy.master.language

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.lingver.Lingver
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentLanguageBinding
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.splash.AgreementDialog
import com.tici.vpn.proxy.master.utils.Navigator
import java.util.Locale


class LanguageFragment : ProductFragment<FragmentLanguageBinding>() {
    private val mViewModel: LanguageViewModel by viewModels()
    private val mLangAdapter = LanguageAdapter()
    private var languageCode = ""

    private var fromSetting: Boolean = false

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater, container, false)
    }

    companion object {
        private const val ARG_FROM_SETTING = "fromSetting"

        fun newInstance(fromSetting: Boolean): LanguageFragment {
            val fragment = LanguageFragment()
            val args = Bundle()
            args.putBoolean(ARG_FROM_SETTING, fromSetting)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromSetting = arguments?.getBoolean(ARG_FROM_SETTING) ?: false
    }

    override fun initView() {
        super.initView()
        mViewModel.init()
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        binding.icBack.scaleX = if (isRTL) -1f else 1f
        if (fromSetting) {
            binding.icBack.visibility = View.VISIBLE
            binding.icBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        } else {
            binding.icBack.visibility = View.GONE
        }
        binding.rcvLanguage.adapter = mLangAdapter
        val datas = ArrayList<LangDataModel>()
        for (lang in LangType.entries) {
            if (lang.langCode.isNotEmpty()) {
                datas.add(LangDataModel(getLanguageName(lang.langCode), lang))
            } else {
                datas.add(getSystemLanguage(lang))
            }
        }
        mLangAdapter.updateData(datas)
        mLangAdapter.focusToLang(BaseAppConfig.languageCode)

        mLangAdapter.onItemClick = { position, item ->
            languageCode = item.langCode
            mLangAdapter.notifyItemChanged(position)
        }

        binding.ivDone.setOnClickNoDoubleClick {

//            if (!BaseAppConfig.allowCollectData && !fromSetting) {
//                AgreementDialog(
//                    requireActivity(),
//                    onContinue = {
            if (languageCode.isEmpty()) {
                BaseAppConfig.firstTimeSetup = false
            } else {
                BaseAppConfig.firstTimeSetup = false
                BaseAppConfig.languageCode = languageCode
                if (BaseAppConfig.languageCode.isNotEmpty()) {
                    Lingver.getInstance().setLocale(requireContext(), BaseAppConfig.languageCode)
                    activity?.recreate()
                } else {
                    Lingver.getInstance().setFollowSystemLocale(requireContext())
                }
            }

            BaseAppConfig.allowCollectData = true
            (activity as? LanguageActivity)?.apply {
                Navigator.startMainActivity(this)
                finish()
            }
        }
//                ).show()
//            } else {
//                (activity as? LanguageActivity)?.apply {
//                    Navigator.startMainActivity(this)
//                    finish()
//                }
//            }
//    }
    }

    private fun getSystemLanguage(lang: LangType): LangDataModel {
        val local = try {
            Resources.getSystem().configuration.locales[0]
        } catch (ex: Exception) {
            Resources.getSystem().configuration.locale
        }

        val isDuplicate = LangType.entries.toTypedArray()
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


    private fun getLanguageName(languageCode: String): String {
        val local = Locale(languageCode)
        val name = local.getDisplayName(local)
        return name.replaceFirstChar { it.uppercase() }
    }
}