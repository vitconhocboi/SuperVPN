package com.tici.vpn.proxy.master.settings

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.BillingViewModel
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import com.core.config.domain.data.IAdPlaceName
import com.core.rate.RateInApp
import com.core.utilities.setOnSingleClick
import com.core.utilities.toast
import com.core.utilities.util.toast.Toasty
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentSettingBinding
import com.tici.vpn.proxy.master.dialog.DialogFeedback
import com.tici.vpn.proxy.master.dialog.SettingSuccessDialog
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.inapp.InAppBillingViewModel
import com.tici.vpn.proxy.master.required.preferences.CoreAppPreferences
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.shareApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment() : BaseFragment<FragmentSettingBinding>() {
    private val inAppPurchasedViewModel: BillingViewModel by viewModels<InAppBillingViewModel>()

    @Inject
    lateinit var coreAppPreferences: CoreAppPreferences

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.SettingFragment

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_BOTTOM_DNS_DEFAULT
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_BOTTOM_DNS_DEFAULT
        )
    }

    override fun initViews(savedInstanceState: Bundle?) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_setting_pro_bg)
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.cornerRadius = 20f  // Adjust your radius
        initEvent()

        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.apply {

            btnAction.scaleX = if (isRTL) -1f else 1f
            btnDnsAction.scaleX = if (isRTL) -1f else 1f
            btnLanguageAction.scaleX = if (isRTL) -1f else 1f
            btnRateAction.scaleX = if (isRTL) -1f else 1f
            btnShareAction.scaleX = if (isRTL) -1f else 1f
            btnFeedbackAction.scaleX = if (isRTL) -1f else 1f
            btnAdsAction.scaleX = if (isRTL) -1f else 1f

            rowAppProxy.setOnClickListener {
                fetchInstalledApps()
            }
            rowDns.setOnClickListener() {
                (activity as? MainActivity)?.showDns()
            }

            btnPro.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
            }

            rowRestore.setOnSingleClick {
                inAppPurchasedViewModel.restorePurchased(false, true)
            }

            rowAdsBlock.setOnClickListener {
                rowAdsBlock.isSelected = !rowAdsBlock.isSelected
                BaseAppConfig.adsBlock = rowAdsBlock.isSelected
                SettingSuccessDialog(R.string.setting_ads_limit).show(
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
        collectFlowOn(inAppPurchasedViewModel.restorePurchaseState) {
            it?.let {
                if (purchasePreferences.isUserVip()) {
                    Toasty.success(requireContext(), this.getString(R.string.text_restore_member))
                        .show()
                } else {
                    Toasty.normal(
                        requireContext(),
                        this.getString(R.string.text_restore_member_failed)
                    ).show()
                }
                updateViewVip()
            }
        }

        collectFlowOn(inAppPurchasedViewModel.vipState) {
            updateViewVip()
        }
    }

    private fun initEvent() {
        binding.viewClickBgClVip.setOnSingleClick {
            if (purchasePreferences.isProByYear) {
                toast(
                    String.format(
                        "%s %s!",
                        getString(R.string.text_congrats_to_join),
                        getString(R.string.text_name_pro),
                    ), Toasty.INFO
                )
            } else {
                Navigator.startPremiumActivity(requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateViewVip()
    }


    fun updateViewVip() {
        if (isAdded && view != null) {
            if (purchasePreferences.isUserVip()) {
                binding.bgClIsVipMember.isVisible = true
                binding.bgClNoneVipMember.isVisible = false
            } else {
                binding.bgClIsVipMember.isVisible = false
                binding.bgClNoneVipMember.isVisible = true
            }
        }
    }

    private fun fetchInstalledApps() {
        openAppProxy()
    }

    private fun openAppProxy() {
//        binding.progress.setVisible(true)
        (activity as? MainActivity)?.showAppProxy()
    }

    fun showProgress(status: Boolean) {
//        binding.progress.setVisible(status)
    }
}