package com.tici.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.core.baseui.BillingViewModel
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import com.core.utilities.setOnSingleClick
import com.core.utilities.util.toast.Toasty
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentPremiumBinding
import com.tici.vpn.proxy.master.required.inapp.InAppBillingViewModel
import com.tici.vpn.proxy.master.required.inapp.ProductIdProviderImpl
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Constant.ModeProTime.MONTH_PRO
import com.tici.vpn.proxy.master.utils.Constant.ModeProTime.WEEK_PRO
import com.tici.vpn.proxy.master.utils.Constant.ModeProTime.YEAR_PRO
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumFragment : BaseFragment<FragmentPremiumBinding>() {

    private val inAppPurchasedViewModel: BillingViewModel by viewModels<InAppBillingViewModel>()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.PremiumFragment

    override fun initViews(savedInstanceState: Bundle?) {
        if (!purchasePreferences.isUserVip()) {
            upDateCheckedViewPro(WEEK_PRO)
        }
        initEvent()
    }

    private fun upDateCheckedViewPro(proTimeType: Int) {
        binding.apply {
            bgClPro1.isActivated = proTimeType == WEEK_PRO
            bgClPro2.isActivated = proTimeType == MONTH_PRO
            bgClPro3.isActivated = proTimeType == YEAR_PRO

            checkBoxPro1.isChecked = proTimeType == WEEK_PRO
            checkBoxPro2.isChecked = proTimeType == MONTH_PRO
            checkBoxPro3.isChecked = proTimeType == YEAR_PRO
        }
    }

    override fun handleObservable() {
        collectFlowOn(inAppPurchasedViewModel.productWeekly) { product ->
            product?.let {
                binding.tvPrice1.text = product.formatPriceVND()
                binding.tvTimePro1.text = product.formatBillingPeriod(requireContext())
                val freeBillingPeriod = product.formatFreeBillingPeriod(requireContext())
                if (freeBillingPeriod != null) {
                    binding.tvTimerFreeTrialPro1.text = freeBillingPeriod
                } else {
                    binding.tvTimerFreeTrialPro1.visibility = View.INVISIBLE
                }
            }
        }

        collectFlowOn(inAppPurchasedViewModel.productMonthly) { product ->
            product?.let {
                binding.tvPrice2.text = product.formatPriceVND()
                binding.tvTimePro2.text = product.formatBillingPeriod(requireContext())
                val freeBillingPeriod = product.formatFreeBillingPeriod(requireContext())
                if (freeBillingPeriod != null) {
                    binding.tvTimerFreeTrialPro2.text = freeBillingPeriod
                }
            }
        }

        collectFlowOn(inAppPurchasedViewModel.productYearly) { product ->
            product?.let {
                binding.tvPrice3.text = product.formatPriceVND()
                binding.tvTimePro3.text = product.formatBillingPeriod(requireContext())
                val freeBillingPeriod = product.formatFreeBillingPeriod(requireContext())
                if (freeBillingPeriod != null) {
                    binding.tvTimerFreeTrialPro3.text = freeBillingPeriod
                }
            }
        }

        collectFlowOn(inAppPurchasedViewModel.newPurchaseAny) { isPurchased ->
            isPurchased?.isNotEmpty()?.let {
                updatePackageEnableState(
                    isProByWeek = purchasePreferences.isProByWeek,
                    isProByMonth = purchasePreferences.isProByMonth,
                    isProByYear = purchasePreferences.isProByYear
                )
                activity?.let {
                    Toasty.success(
                        it, String.format(
                            "%s %s! %s",
                            getString(R.string.text_congrats_to_join),
                            getString(R.string.text_name_pro),
                            getString(R.string.text_enjoy_full_experience_now)
                        ), Toasty.INFO
                    ).show()

                    it.finish()
                }
            }
        }

        collectFlowOn(inAppPurchasedViewModel.restorePurchaseState) { restoreState ->
            restoreState?.let {
                if (purchasePreferences.isUserVip()) {
                    Toasty.success(requireContext(), this.getString(R.string.text_restore_member))
                        .show()

                    activity?.finish()
                } else {
                    Toasty.normal(
                        requireContext(),
                        this.getString(R.string.text_restore_member_failed)
                    ).show()
                }
            }
        }

        updatePackageEnableState(
            isProByWeek = purchasePreferences.isProByWeek,
            isProByMonth = purchasePreferences.isProByMonth,
            isProByYear = purchasePreferences.isProByYear
        )
    }

    private fun setPackageState(
        layout: View,
        checkbox: View,
        enabled: Boolean,
        activated: Boolean,
        checked: Boolean,
    ) {
        layout.isEnabled = enabled
        layout.isActivated = activated
        layout.alpha = if (enabled) 1f else 0.5f
        checkbox.isEnabled = enabled
        if (checkbox is android.widget.CheckBox) checkbox.isChecked = checked
    }

    private fun updatePackageEnableState(
        isProByWeek: Boolean,
        isProByMonth: Boolean,
        isProByYear: Boolean,
    ) {
        var enableWeek = false
        var enableMonth = false
        var enableYear = false
        var activateWeek = false
        var activateMonth = false
        var activateYear = false
        var checkWeek = false
        var checkMonth = false
        var checkYear = false

        when {
            !isProByWeek && !isProByMonth && !isProByYear -> {
                // Chưa mua gì, chọn tuần
                enableWeek = true
                enableMonth = true
                enableYear = true
                activateWeek = true
                checkWeek = true
            }

            isProByWeek && !isProByMonth && !isProByYear -> {
                // Đã mua tuần, chọn tháng
                enableWeek = false
                enableMonth = true
                enableYear = true
                activateMonth = true
                checkMonth = true
            }

            isProByMonth && !isProByYear -> {
                // Đã mua tháng, chọn năm
                enableWeek = false
                enableMonth = false
                enableYear = true
                activateYear = true
                checkYear = true
            }

            else -> {
                // Đã mua năm, chỉ chọn năm
                enableWeek = false
                enableMonth = false
                enableYear = true
                activateYear = true
                checkYear = true
            }
        }

        setPackageState(
            binding.bgClPro1, binding.checkBoxPro1,
            enabled = enableWeek,
            activated = activateWeek,
            checked = checkWeek
        )
        setPackageState(
            binding.bgClPro2, binding.checkBoxPro2,
            enabled = enableMonth,
            activated = activateMonth,
            checked = checkMonth
        )
        setPackageState(
            binding.bgClPro3, binding.checkBoxPro3,
            enabled = enableYear,
            activated = activateYear,
            checked = checkYear
        )
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        binding.root.setOnClickListener {
            return@setOnClickListener
        }

        binding.root.setOnTouchListener { v, event ->
            return@setOnTouchListener false
        }

        binding.btnClose.setOnSingleClick {
            activity?.finish()
        }

        binding.bgClPro1.setOnSingleClick {
            upDateCheckedViewPro(WEEK_PRO)
        }

        binding.bgClPro2.setOnSingleClick {
            upDateCheckedViewPro(MONTH_PRO)
        }

        binding.bgClPro3.setOnSingleClick {
            upDateCheckedViewPro(YEAR_PRO)
        }

        binding.tvRestorePurchase.setOnSingleClick {
            inAppPurchasedViewModel.restorePurchased(false, true)
        }

        binding.tvBuyPremium.setOnSingleClick {
            when {
                binding.checkBoxPro1.isChecked -> {
                    inAppPurchasedViewModel.launchBillingFlow(
                        requireActivity(),
                        ProductIdProviderImpl.PRO_BY_WEEK
                    )
                }

                binding.checkBoxPro2.isChecked -> {
                    inAppPurchasedViewModel.launchBillingFlow(
                        requireActivity(),
                        ProductIdProviderImpl.PRO_BY_MONTH
                    )
                }

                binding.checkBoxPro3.isChecked -> {
                    inAppPurchasedViewModel.launchBillingFlow(
                        requireActivity(),
                        ProductIdProviderImpl.PRO_BY_YEAR
                    )
                }
            }
        }
    }
}