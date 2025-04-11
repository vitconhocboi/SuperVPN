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
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.R
import com.akmobile.supervpn.databinding.FragmentSettingBinding
import com.akmobile.supervpn.settings.appproxy.AppProxyUI
import com.akmobile.supervpn.settings.appproxy.AppProxyFragment
import com.akmobile.supervpn.utils.Constant
import com.akmobile.supervpn.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : ProductFragment<FragmentSettingBinding>() {

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

        mViewModel.loadingApps.observe(viewLifecycleOwner) {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        binding.apply {

            premium.background = roundedDrawable

            rowAppProxy.setOnClickListener {
                fetchInstalledApps()
            }

            btnPro.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
            }
        }
    }

    private fun fetchInstalledApps() {
        lifecycleScope.launch {
            mViewModel.loadingApp()
            val installedApps = getInstalledAppsWithInternetPermission()
            mViewModel.loadingAppDone()
            openAppProxy(installedApps)
        }
    }

    private fun getInstalledAppsWithInternetPermission(): ArrayList<AppProxyUI> {
        val packageManager = requireContext().packageManager
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { appInfo ->
                if (packageManager.checkPermission(
                        android.Manifest.permission.INTERNET, appInfo.packageName
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val appIcon = packageManager.getApplicationIcon(appInfo)
                    AppProxyUI(appInfo.packageName, appIcon, appName)
                } else null
            } as ArrayList<AppProxyUI>
    }

    private fun openAppProxy(list: ArrayList<AppProxyUI>) {
        childFragmentManager.beginTransaction()
            .replace(R.id.frame_container, AppProxyFragment(list))
            .addToBackStack(null)
            .commit()
    }
}