package com.akmobile.supervpn.settings

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.FragmentSettingBinding
import com.akmobile.supervpn.settings.appproxy.AppProxyUI
import com.akmobile.supervpn.settings.appproxy.AppProxyFragment
import com.akmobile.supervpn.utils.Constant
import com.akmobile.supervpn.utils.Navigator
import com.common.baseui.BaseAppConfig
import com.simple.libads.setVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment(
    val showAppProxy: () -> Unit,
    val showDns: () -> Unit = {}
) : ProductFragment<FragmentSettingBinding>() {

    private val mViewModel: SettingViewModel by viewModels()

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
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
                Navigator.startLanguageActivity(requireContext())
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