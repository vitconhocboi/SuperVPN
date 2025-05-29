package com.akmobile.supervpn.proxy

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentProxyBinding
import com.akmobile.supervpn.utils.Navigator
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.setVisible
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PremiumProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    private val mProxyViewModel: ProxyViewModel by viewModels()

    companion object {
        private var selected = false
        private const val TYPE = "premium"
    }


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProxyBinding {
        return FragmentProxyBinding.inflate(inflater, container, false)
    }

    @SuppressLint("HardwareIds", "NotifyDataSetChanged")
    override fun initView() {
        super.initView()
        with(binding) {
//            tvFree.isSelected = true
//            tvFree.setOnClickListener {
//                tvPremium.isSelected = false
//                tvFree.isSelected = true
//            }
//
//            tvPremium.setOnClickListener {
//                tvFree.isSelected = false
//                tvPremium.isSelected = true
//            }
//            ivBack.setOnClickListener {
//                Navigator.startMainActivity(requireActivity())
//            }
//            ivReload.setOnClickListener {
//
//            }
            rcvGroupProxy.adapter = mPagerAdapter
            mProxyViewModel.getAllProxy(requireContext(), TYPE)
//            bindFlowCreate(mProxyViewModel.allProxy) { result ->
//                processResultData(result, onSuccess = {
//                    mPagerAdapter.updateData(it)
//                })
//            }
            bindFlowCreate(mProxyViewModel.allProxy) { result ->
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
                lifecycleScope.launch {
                    try {
                        if (!selected) {
                            selected = true
                            mPagerAdapter.notifyDataSetChanged()
                            val deviceId = try {
                                val adInfo =
                                    AdvertisingIdClient.getAdvertisingIdInfo(requireContext())
                                if (!adInfo.isLimitAdTrackingEnabled) {
                                    adInfo.id
                                } else {
                                    Settings.Secure.getString(
                                        requireContext().contentResolver,
                                        Settings.Secure.ANDROID_ID
                                    )
                                }
                            } catch (e: Exception) {
                                // Fallback to Android ID if advertising ID is not available
                                Settings.Secure.getString(
                                    requireContext().contentResolver,
                                    Settings.Secure.ANDROID_ID
                                )
                            }
                            val reconnect =
                                if (item.country != BaseAppConfig.proxyCountry || !item.active) "RECONNECT" else ""
                            if (item.country == BaseAppConfig.proxyCountry) {
                                item.active = false
                            }
                            if (item.active) {
                                mProxyViewModel.setActiveProxy(item, deviceId, TYPE)
                            } else {
                                mProxyViewModel.setActiveProxy(null, deviceId, TYPE)
                            }
                            requireActivity().finish()
                            Navigator.startMainActivity(requireContext(), reconnect)
                            selected = false
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
        }
    }
}