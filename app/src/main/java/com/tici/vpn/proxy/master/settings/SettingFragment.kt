package com.tici.vpn.proxy.master.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.rate.RateInApp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentSettingBinding
import com.tici.vpn.proxy.master.dialog.DialogFeedback
import com.tici.vpn.proxy.master.dialog.SettingSuccessDialog
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.preferences.CoreAppPreferences
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.settings.adsblock.AdsRuleReapplier
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.shareApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    @Inject
    lateinit var adsRuleReapplier: AdsRuleReapplier

    @Inject
    lateinit var coreAppPreferences: CoreAppPreferences

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.SettingFragment

    override fun initViews(savedInstanceState: Bundle?) {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.apply {

            btnAction.scaleX = if (isRTL) -1f else 1f
            btnDnsAction.scaleX = if (isRTL) -1f else 1f
            btnLanguageAction.scaleX = if (isRTL) -1f else 1f
            btnRateAction.scaleX = if (isRTL) -1f else 1f
            btnShareAction.scaleX = if (isRTL) -1f else 1f
            btnFeedbackAction.scaleX = if (isRTL) -1f else 1f
            btnAdsAction.scaleX = if (isRTL) -1f else 1f
            btnAdsRulesAction.scaleX = if (isRTL) -1f else 1f

            rowAppProxy.setOnClickListener {
                fetchInstalledApps()
            }
            rowDns.setOnClickListener {
                (activity as? MainActivity)?.showDns()
            }

            // Reflect the stored master switch; previously the row always rendered as off.
            rowAdsBlock.isSelected = BaseAppConfig.adsBlock
            rowAdsBlockRules.setOnClickListener {
                (activity as? MainActivity)?.showAdsBlock()
            }

            rowAdsBlock.setOnClickListener {
                rowAdsBlock.isSelected = !rowAdsBlock.isSelected
                BaseAppConfig.adsBlock = rowAdsBlock.isSelected
                // Applies to a running filter immediately; no reconnect needed.
                adsRuleReapplier.notifyRulesChanged()
                val title = if (rowAdsBlock.isSelected) R.string.setting_ads_limit else R.string.setting_ads_off
                SettingSuccessDialog(title, R.string.ads_rules_applied).show(
                    childFragmentManager,
                    "SettingSuccessDialog"
                )
            }

            if (BaseAppConfig.dnsServer.isNotEmpty()) {
                tvDnsStatus.text = getString(R.string.status_on)
                tvDnsStatus.setTextColor(resources.getColor(com.common.baseui.R.color.successColor))
            } else {
                tvDnsStatus.text = getString(R.string.status_off)
                tvDnsStatus.setTextColor(resources.getColor(R.color.text_gray_2))
            }

            rowLanguage.setOnClickListener {
                Navigator.startLanguageActivity(requireContext(), fromSetting = true)
            }

            rowRate.setOnClickNoDoubleClick {
                activity?.let {
                    RateInApp.instance.showDialogRateAndFeedback(
                        context = it,
                        onRated = {
                            Firebase.analytics.logEvent("rate_app", Bundle())
                            coreAppPreferences.isEnableRateLogic = false
                        },
                        forceShow = true
                    )
                }
            }

            rowShare.setOnClickNoDoubleClick {
                requireActivity().shareApp()
            }

            rowFeedback.setOnClickNoDoubleClick {
                DialogFeedback().show(childFragmentManager, "DialogFeedback")
            }

        }
    }

    override fun handleObservable() {
    }

    private fun fetchInstalledApps() {
        openAppProxy()
    }

    private fun openAppProxy() {
        (activity as? MainActivity)?.showAppProxy()
    }

    fun showProgress(status: Boolean) {
    }
}
