package com.tici.vpn.proxy.master.settings

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.viewModels
import com.tici.vpn.proxy.master.dialog.DialogFeedback
import com.tici.vpn.proxy.master.dialog.DialogRate
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentSettingBinding
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.shareApp
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.tici.vpn.proxy.master.dialog.SettingSuccessDialog
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.main.SharedData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment(
    val showAppProxy: () -> Unit, val showDns: () -> Unit = {}
) : ProductFragment<FragmentSettingBinding>() {

    private val mViewModel: SettingViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        super.initView()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_setting_pro_bg)
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.cornerRadius = 20f  // Adjust your radius

//        mViewModel.loadingApps.observe(viewLifecycleOwner) {
//            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
//        }

        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.apply {

            btnAction.scaleX = if (isRTL) -1f else 1f
            btnDnsAction.scaleX = if (isRTL) -1f else 1f
            btnLanguageAction.scaleX = if (isRTL) -1f else 1f
            btnRateAction.scaleX = if (isRTL) -1f else 1f
            btnShareAction.scaleX = if (isRTL) -1f else 1f
            btnFeedbackAction.scaleX = if (isRTL) -1f else 1f
            btnAdsAction.scaleX = if (isRTL) -1f else 1f

            if (mainViewModel.isSub()) {
                premium.visibility = View.GONE
            } else {
                premium.background = roundedDrawable
            }

            rowAppProxy.setOnClickListener {
                if (mainViewModel.isSub()) {
                    fetchInstalledApps()
                } else {
                    Navigator.startPremiumActivity(requireContext())
                }
            }
            rowDns.setOnClickListener() {
                showDns()
            }

            btnPro.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
            }

            rowAdsBlock.setOnClickListener {
                if (mainViewModel.isSub()) {
                    rowAdsBlock.isSelected = !rowAdsBlock.isSelected
                    BaseAppConfig.adsBlock = rowAdsBlock.isSelected
                    SettingSuccessDialog(R.string.setting_ads_limit).show(
                        childFragmentManager,
                        "SettingSuccessDialog"
                    )
                } else {
                    Navigator.startPremiumActivity(requireContext())
                }
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
//                BaseAppConfig.isSub = false
//                SharedData.isSub.postValue(false)
                DialogRate().show(childFragmentManager, "DialogRate")
            }

            rowShare.setOnClickNoDoubleClick {
                requireActivity().shareApp()
            }

            rowFeedback.setOnClickNoDoubleClick {
                DialogFeedback().show(childFragmentManager, "DialogFeedback")
            }

            SharedData.isSub.observe(requireActivity()) {
                if (mainViewModel.isSub()) {
                    premium.visibility = View.GONE
                } else {
                    premium.background = roundedDrawable
                }
            }
        }
    }

    private fun fetchInstalledApps() {
        openAppProxy()
    }

    private fun openAppProxy() {
//        binding.progress.setVisible(true)
        showAppProxy()
    }

    fun showProgress(status: Boolean) {
//        binding.progress.setVisible(status)
    }
}