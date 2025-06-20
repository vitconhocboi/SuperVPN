package com.highsecure.vpn.proxy.master.proxy

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.FragmentProxyBinding
import com.highsecure.vpn.proxy.master.utils.Navigator
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.setVisible
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.highsecure.vpn.proxy.master.main.MainViewModel
import com.highsecure.vpn.proxy.master.proxy.ProxyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PremiumProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    private val mProxyViewModel: ProxyViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    companion object {
        private var selected = false
        private const val PREMIUM = "premium"
//        private const val FREE = "free"
    }


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProxyBinding {
        return FragmentProxyBinding.inflate(inflater, container, false)
    }

    @SuppressLint("HardwareIds", "NotifyDataSetChanged")
    override fun initView() {
        super.initView()
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        with(binding) {
            ivBack.scaleX = if (isRTL) -1f else 1f
            rcvGroupProxy.adapter = mPagerAdapter
            mProxyViewModel.getAllPremiumProxy(requireContext(), PREMIUM)
            bindFlowCreate(mProxyViewModel.allPremiumProxy) { result ->
                when (result.status) {
                    ResultData.State.STANDBY -> {

                    }

                    ResultData.State.LOADING -> {
                        progress.setVisible(true)
                    }

                    ResultData.State.SUCCESS -> {
                        val data = result.data ?: arrayListOf()
                        if (data.isNotEmpty()) {
                            mPagerAdapter.updateData(data)
                            progress.setVisible(false)
                        }
                    }

                    ResultData.State.ERROR -> {
                        progress.setVisible(false)
                        Toast.makeText(
                            requireContext(),
                            "Cannot get proxy",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            mPagerAdapter.onItemClick = { _, item ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (!selected) {
                            if (mainViewModel.isSub()) {
                                selected = true
                                mPagerAdapter.notifyDataSetChanged()
                                val deviceId = mainViewModel.getDeviceId(requireContext())
                                val reconnect =
                                    if (item.country != BaseAppConfig.proxyCountry || !item.active) "RECONNECT" else ""
                                if (item.country == BaseAppConfig.proxyCountry) {
                                    item.active = false
                                }
                                try {
                                    if (item.active) {
                                        mProxyViewModel.setActiveProxy(item, deviceId, PREMIUM)
                                    } else {
                                        mProxyViewModel.setActiveProxy(null, deviceId, PREMIUM)
                                    }
                                    requireActivity().finish()
                                    Navigator.startMainActivity(requireContext(), "POPUP")
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), "An error occur. Please try again", Toast.LENGTH_SHORT).show()
                                }
                                selected = false
                            } else {
                                Navigator.startPremiumActivity(requireContext())
                            }
                        } else {
                            item.active = false
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Cannot get proxy: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity())
            }
        }
    }
}