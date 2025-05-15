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
import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.settings.appproxy.AppProxyViewModel
import com.akmobile.supervpn.utils.Navigator
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.setVisible
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    private val mProxyViewModel: ProxyViewModel by viewModels()


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProxyBinding {
        return FragmentProxyBinding.inflate(inflater, container, false)
    }

    @SuppressLint("HardwareIds")
    override fun initView() {
        super.initView()
        with(binding) {
            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity(), null)
            }
            ivReload.setOnClickListener {

            }
            rcvGroupProxy.adapter = mPagerAdapter
            mProxyViewModel.getAllProxy(requireContext())
//            bindFlowCreate(mProxyViewModel.allProxy) { result ->
//                processResultData(result, onSuccess = {
//                    mPagerAdapter.updateData(it)
//                })
//            }

            mProxyViewModel.allProxy.observe(viewLifecycleOwner) { it ->
                if (it.isNotEmpty()) {
                    mPagerAdapter.updateData(it)
                }
                progress.setVisible(false)
            }

            mPagerAdapter.onItemClick = { _, item ->
                lifecycleScope.launch {
                    try {
                        val deviceId = try {
                            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(requireContext())
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

                        if (item.active) {
                            mProxyViewModel.setActiveProxy(item, deviceId)
                        } else {
                            mProxyViewModel.setActiveProxy(null, deviceId)
                        }
                        mPagerAdapter.notifyDataSetChanged()
                        Navigator.startMainActivity(requireContext(), "")
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Cannot get proxy: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}