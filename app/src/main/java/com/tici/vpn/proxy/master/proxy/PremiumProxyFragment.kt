package com.tici.vpn.proxy.master.proxy

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.utils.Navigator
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.setVisible
import com.tici.vpn.proxy.master.databinding.FragmentProxyBinding
import com.tici.vpn.proxy.master.dialog.DialogRateComplete
import com.tici.vpn.proxy.master.dialog.UnlockDialog
import com.tici.vpn.proxy.master.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PremiumProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    @Inject
    lateinit var mQuickAccessAdapter: ProxyGroupAdapter

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
//        Log.i("TestRelease", "getAllPremiumProxy initView")
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        with(binding) {
            ivBack.scaleX = if (isRTL) -1f else 1f
            rcvGroupProxy.adapter = mPagerAdapter

            rcvQuickAccess.adapter = mQuickAccessAdapter

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
                            if (mProxyViewModel.getLastUsedVpn().isNotEmpty()) {
                                tvQuickAccess.visibility = View.VISIBLE
                                mQuickAccessAdapter.updateData(data.filter { mProxyViewModel.getLastUsedVpn().contains(it.country) })
                            } else {
                                tvQuickAccess.visibility = View.GONE
                            }
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

            mQuickAccessAdapter.onItemClick = { _, item ->
                try {
                    lifecycleScope.launch {
                        if (!selected) {
                            if (item.type == "free" || mainViewModel.isSub()) {
                                selected = true
                                mQuickAccessAdapter.notifyDataSetChanged()
                                val deviceId = mainViewModel.getDeviceId(requireContext())
                                val reconnect =
                                    if (item.country != BaseAppConfig.proxyCountry || !item.active) "RECONNECT" else ""
                                if (item.country == BaseAppConfig.proxyCountry) {
                                    item.active = false
                                }
                                try {
                                    BaseAppConfig.usingType = item.type
                                    if (item.active) {
                                        mProxyViewModel.setActiveProxy(item, deviceId, item.type)
                                    } else {
                                        mProxyViewModel.setActiveProxy(null, deviceId, item.type)
                                    }
                                    requireActivity().finish()
                                    Navigator.startMainActivity(requireContext(), "POPUP")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "An error occur. Please try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                selected = false
                            } else {
//                                Navigator.startPremiumActivity(requireContext())
                                UnlockDialog(data = item).show(parentFragmentManager, "unlock_dialog")
                            }
                        } else {
                            item.active = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Cannot get proxy: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            mPagerAdapter.onItemClick = { _, item ->
                try {
                    lifecycleScope.launch {
                        Timber.d("click country item 1 item country ${item.country}")
                        if (!selected) {
                            Timber.d("click country item 1 1")
                            if (item.type == "free" || mainViewModel.isSub()) {
                                Timber.d("click country item 2 ${BaseAppConfig.proxyCountry} ${item.country}")
                                selected = true
                                mPagerAdapter.notifyDataSetChanged()
                                val deviceId = mainViewModel.getDeviceId(requireContext())
                                val reconnect =
                                    if (item.country != BaseAppConfig.proxyCountry || !item.active) "RECONNECT" else ""
                                if (item.country == BaseAppConfig.proxyCountry) {
                                    item.active = false
                                }
                                try {
                                    Timber.d("click country item 2 ${BaseAppConfig.proxyCountry} ${item.country} ${item.active}")
                                    if (item.active) {
                                        mProxyViewModel.setActiveProxy(item, deviceId, item.type)
                                    } else {
                                        mProxyViewModel.setActiveProxy(null, deviceId, item.type)
                                    }
                                    requireActivity().finish()
                                    Navigator.startMainActivity(requireContext(), "POPUP")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        requireContext(),
                                        "An error occur. Please try again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                selected = false
                            } else {
//                                Navigator.startPremiumActivity(requireContext())
                                UnlockDialog(data = item).show(parentFragmentManager, "unlock_dialog")
                            }
                        } else {
                            item.active = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Cannot get proxy: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity())
            }
        }

        mProxyViewModel.isError.observe(viewLifecycleOwner) { isError ->
            Timber.d("isError PremiumFragment $isError")
            if (isError == true) {
                Toast.makeText(requireContext(), "Connection error PremiumFragment. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}