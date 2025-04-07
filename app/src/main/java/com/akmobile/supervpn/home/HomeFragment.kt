package com.akmobile.supervpn.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentHomeBinding
import com.akmobile.supervpn.network.LocalVpnService
import com.akmobile.supervpn.proxy.ProxyViewModel
import com.akmobile.supervpn.utils.Navigator
import com.akmobile.supervpn.utils.Utils
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.gone
import com.common.baseui.extension.invisible
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.visible
import com.simple.libads.gone
import dagger.hilt.android.AndroidEntryPoint
import com.akmobile.supervpn.R
import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.scheduler.StopProxyScheduler
import com.akmobile.supervpn.settings.appproxy.AppProxyViewModel

@AndroidEntryPoint
class HomeFragment : ProductFragment<FragmentHomeBinding>() {

    companion object {
        public const val CONNECTING = "CONNECTING"
        public const val CONNECTED = "CONNECTED"
        public const val DISCONNECTING = "DISCONNECTING"
        public const val DISCONNECTED = "DISCONNECTED"
        public const val ERROR = "ERROR"
    }

    private var isConnected = false
    private var proxyId: String? = ""
    private val proxyViewModel: ProxyViewModel by viewModels()

    private val allowAppViewModel: AppProxyViewModel by viewModels()

    private var allowApp: List<VpnAppItemDB>? = null

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startVpnService()
            }
        }

//    private val vpnStatusReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val status = intent?.getStringExtra(SupperVpnService.EXTRA_VPN_STATUS)
//            updateUI(status)
//        }
//    }

    private fun updateUI(status: String?) {
        when (status) {
            CONNECTING -> {
                binding.tvStatus.text = activity?.resources?.getString(R.string.connecting)
                binding.ivConnect.isEnabled = false
            }

            CONNECTED -> {
                binding.ivConnect.isSelected = true
                isConnected = true
                binding.tvStatus.text = activity?.resources?.getString(R.string.connected)
                binding.ivConnect.isEnabled = true
            }

            DISCONNECTING -> {
                binding.tvStatus.text = activity?.resources?.getString(R.string.disconnecting)
                binding.ivConnect.isEnabled = false
            }

            DISCONNECTED -> {
                isConnected = false
                binding.ivConnect.isSelected = false
                binding.tvStatus.text = activity?.resources?.getString(R.string.disconnected)
                binding.ivConnect.isEnabled = true
            }

            ERROR -> {
                binding.ivConnect.isSelected = false
                isConnected = false
                binding.tvStatus.text = activity?.resources?.getString(R.string.connect_error)
                binding.ivConnect.isEnabled = true
            }
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        // Đăng ký BroadcastReceiver
//        context?.let {
//            LocalBroadcastManager.getInstance(it).registerReceiver(
//                vpnStatusReceiver, IntentFilter(LocalVpnService.ACTION_VPN_STATUS)
//            )
//        }
        with(binding) {
            proxyViewModel.getActiveProxy()
            bindFlowCreate(proxyViewModel.activeProxy) {
                processResultData(it, onSuccess = { it ->
                    if (it != null) {
                        //set to pref
                        BaseAppConfig.proxyType = it.type
                        BaseAppConfig.proxyHost = it.host
                        BaseAppConfig.proxyPort = it.port
                        BaseAppConfig.proxyUser = it.username
                        BaseAppConfig.proxyPass = it.password
                        //update UI
                        ivFlag.setImageResource(Utils.getFlag(it.country))
                        tvProxyLocation.text = it.name
                        tvProxyIp.text = it.host
                        pnProxyInfo.visible()
                        tvSelectProxy.invisible()
                    } else {
                        BaseAppConfig.proxyType = ""
                        BaseAppConfig.proxyHost = ""
                        BaseAppConfig.proxyPort = ""
                        BaseAppConfig.proxyUser = ""
                        BaseAppConfig.proxyPass = ""

                        pnProxyInfo.invisible()
                        tvSelectProxy.visible()
                    }
                })
            }

            allowAppViewModel.getAllowApp()

            bindFlowCreate(allowAppViewModel.allowApp) { result ->
                processResultData(result, onSuccess = {
                    allowApp = it
                })
            }

            ivConnect.setOnClickListener {
//                if (isConnected) {
//                    stopVpnService()
//                } else {
//                    ChooseTimeDialog(
//                        requireActivity(),
//                        onSelect = { min ->
//                            StopProxyScheduler.runProxy(requireContext(), min) {
//                                prepareVpn()
//                            }
//                        }
//                    ).show()
//                }
                if (!isConnected) {
                    prepareVpn()
                } else {
                    stopVpnService()
                }
            }

            pnProxy.setOnClickListener {
                Navigator.startProxyActivity(requireContext(), null)
            }

            proxyId = arguments?.getString("id", null)
        }

        proxyViewModel.isConnected.observe(viewLifecycleOwner) { status ->
            updateUI(status)
        }

    }

    private fun prepareVpn() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        isConnected = true
        proxyViewModel.startProxy(context, allowApp = allowApp?.map { it.packageName })
    }

    private fun stopVpnService() {
        try {
            proxyViewModel.stopProxy(context)
//            LocalVpnService.IsRunning = false
//            isConnected = false
//            binding.ivConnect.isSelected = false
//            binding.tvStatus.text = activity?.resources?.getString(R.string.disconnecting)
        } catch (e: Exception) {
            Log.e("VPN", "Error stopping VPN service", e)
        }
    }
}