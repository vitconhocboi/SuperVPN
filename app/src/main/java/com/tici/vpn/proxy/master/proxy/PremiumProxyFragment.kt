package com.tici.vpn.proxy.master.proxy

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.ResultData
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.setVisible
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentProxyBinding
import com.tici.vpn.proxy.master.dialog.UnlockDialog
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Constant.KEY_RESULT_CONNECT_VPN
import com.tici.vpn.proxy.master.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PremiumProxyFragment : BaseFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter

    @Inject
    lateinit var mQuickAccessAdapter: ProxyGroupAdapter

    private val mProxyViewModel: ProxyViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var selectedItem: ProxyGroupUI
    private var selectedType: Int = 0

    private var connectState: String = ""

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

    override val screenType: ScreenType
        get() = AppScreenType.PremiumProxyFragment

    @SuppressLint("HardwareIds", "NotifyDataSetChanged")
    override fun initViews(savedInstanceState: Bundle?) {
//        Log.i("TestRelease", "getAllPremiumProxy initView")

        callBackResult()
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
                                mQuickAccessAdapter.updateData(data.filter {
                                    mProxyViewModel.getLastUsedVpn().contains(it.country)
                                })
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
                            getString(R.string.network_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            mQuickAccessAdapter.onItemClick = { _, item ->
                selectedItem = item
                selectedType = 1
                if (item.active) {
                    btnConnect.visibility = View.VISIBLE
                } else {
                    btnConnect.visibility = View.GONE
                }
                mPagerAdapter.datas.find { it?.country == item.country }?.active = item.active
                mQuickAccessAdapter.notifyDataSetChanged()
                mPagerAdapter.notifyDataSetChanged()
            }

            mPagerAdapter.onItemClick = { _, item ->
                selectedItem = item
                selectedType = 2
                if (item.active) {
                    btnConnect.visibility = View.VISIBLE
                } else {
                    btnConnect.visibility = View.GONE
                }
                mQuickAccessAdapter.datas.find { it?.country == item.country }?.active = item.active
                mQuickAccessAdapter.notifyDataSetChanged()
                mPagerAdapter.notifyDataSetChanged()
            }

            btnConnect.setOnClickListener {
                try {
                    lifecycleScope.launch {
                        showLoading()
                        try {
                            if (selectedItem.active) {
                                if (selectedItem.type != "free" && !mainViewModel.isSub()) {
                                    UnlockDialog(data = selectedItem).show(
                                        childFragmentManager,
                                        UnlockDialog::class.java.simpleName
                                    )
                                    return@launch
                                }
                            }
                            connectState =
                                if (selectedItem.country != BaseAppConfig.proxyCountry || !selectedItem.active || !LocalVpnService.IsRunning) {
                                    if (LocalVpnService.IsRunning) "RECONNECT" else "CONNECT"
                                } else ""
                            val deviceId = mainViewModel.getDeviceId(requireContext())
                            if (selectedItem.active) {
                                mProxyViewModel.setActiveProxy(
                                    selectedItem,
                                    deviceId,
                                    selectedItem.type
                                )
                            } else {
                                mProxyViewModel.setActiveProxy(
                                    null,
                                    deviceId,
                                    selectedItem.type
                                )
                            }
                            requireActivity().finish()
                            Navigator.startMainActivity(
                                requireContext(),
                                connectState
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.network_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    hideLoading()
                }
            }

            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity())
                activity?.finish()
            }
        }

        mProxyViewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                Toast.makeText(
                    requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun connectVPN() {
        try {
            Timber.tag("SuperVPN").d("SuperVPN come here showLoading 1")
            lifecycleScope.launch {
                Timber.tag("SuperVPN").d("SuperVPN come here showLoading 2")
                showLoading()
                try {
                    connectState =
                        if (selectedItem.country != BaseAppConfig.proxyCountry || !selectedItem.active || !LocalVpnService.IsRunning) {
                            if (LocalVpnService.IsRunning) "RECONNECT" else "CONNECT"
                        } else ""
                    val deviceId = mainViewModel.getDeviceId(requireContext())
                    Timber.tag("SuperVPN")
                        .d("SuperVPN come here showLoading 2 $deviceId ${selectedItem.active}")
                    if (selectedItem.active) {
                        mProxyViewModel.setActiveProxy(
                            selectedItem,
                            deviceId,
                            selectedItem.type
                        )
                    } else {
                        mProxyViewModel.setActiveProxy(
                            null,
                            deviceId,
                            selectedItem.type
                        )
                    }
                    requireActivity().finish()
                    Timber.tag("SuperVpn").i("startMainActivity with state $connectState")
                    Navigator.startMainActivity(
                        requireContext(),
                        connectState
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "An error occur. Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Cannot get proxy: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            hideLoading()
        }
    }

    private fun callBackResult() {
        childFragmentManager.setFragmentResultListener(
            KEY_RESULT_CONNECT_VPN,
            this
        ) { _, bundle ->
            if (view != null && isAdded) {
                connectVPN()
            }
        }
    }
}