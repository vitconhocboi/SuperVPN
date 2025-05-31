package com.highsecure.vpn.proxy.master.settings

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.viewModels
import com.highsecure.vpn.proxy.master.dialog.DialogFeedback
import com.highsecure.vpn.proxy.master.dialog.DialogRate
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.FragmentSettingBinding
import com.highsecure.vpn.proxy.master.utils.Navigator
import com.highsecure.vpn.proxy.master.utils.shareApp
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setOnClickNoDoubleClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment(
    val showAppProxy: () -> Unit, val showDns: () -> Unit = {}
) : ProductFragment<FragmentSettingBinding>() {

    private val mViewModel: SettingViewModel by viewModels()


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

        binding.apply {

            premium.background = roundedDrawable

            rowAppProxy.setOnClickListener {
                fetchInstalledApps()
            }
            rowDns.setOnClickListener() {
                showDns()
            }

            btnPro.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
            }

            rowAdsBlock.setOnClickListener {
                rowAdsBlock.isSelected = !rowAdsBlock.isSelected
                BaseAppConfig.adsBlock = rowAdsBlock.isSelected
            }

            if (BaseAppConfig.dnsServer.isNotEmpty()) {
                tvDnsStatus.text = getString(R.string.status_on)
            } else {
                tvDnsStatus.text = getString(R.string.status_off)
            }

            rowLanguage.setOnClickListener {
                Navigator.startLanguageActivity(requireContext(), fromSetting = true)
            }

            rowRate.setOnClickNoDoubleClick {
                DialogRate().show(childFragmentManager, "DialogRate")
            }

            rowShare.setOnClickNoDoubleClick {
                requireActivity().shareApp()
            }

            rowFeedback.setOnClickNoDoubleClick {
                DialogFeedback().show(childFragmentManager, "DialogFeedback")
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