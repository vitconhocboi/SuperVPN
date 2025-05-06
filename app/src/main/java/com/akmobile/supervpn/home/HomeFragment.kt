package com.akmobile.supervpn.home

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope

import com.akmobile.supervpn.base.ProductFragment
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
import com.akmobile.supervpn.network.ProxySpeedTest
import com.akmobile.supervpn.scheduler.StopProxyScheduler
import com.akmobile.supervpn.settings.appproxy.AppProxyViewModel
import com.common.baseui.extension.context
import com.simple.libads.setVisible

@AndroidEntryPoint
class HomeFragment : ProductFragment<FragmentHomeBinding>() {
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        public const val CONNECTING = "CONNECTING"
        public const val CONNECTED = "CONNECTED"
        public const val DISCONNECTING = "DISCONNECTING"
        public const val DISCONNECTED = "DISCONNECTED"
        public const val ERROR = "ERROR"
    }

    private val interval = 1000L

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

    private fun updateUI(status: String?) {
        when (status) {
            CONNECTING -> {
                binding.connectTitle.invisible()
                binding.tvTime.text = "00:00:00"
                binding.tvTime.visible()
                binding.lnDisconnected.invisible()
                binding.lnConnecting.visible()
                binding.ivConnect.isEnabled = false
            }

            CONNECTED -> {
                binding.ivConnect.isSelected = true
                isConnected = true
                binding.lnConnecting.invisible()
                binding.lnConnected.visible()
                binding.ivConnect.isEnabled = true
            }

            DISCONNECTING -> {
                binding.lnConnected.invisible()
                binding.lnDisconnecting.visible()
                binding.ivConnect.isEnabled = false
            }

            DISCONNECTED -> {
                isConnected = false
                binding.tvTime.invisible()
                binding.connectTitle.visible()
                binding.ivConnect.isSelected = false
                binding.lnDisconnecting.invisible()
                binding.lnDisconnected.visible()
                binding.ivConnect.isEnabled = true
            }

            ERROR -> {
                binding.ivConnect.isSelected = false
                isConnected = false
                binding.lnConnecting.invisible()
                binding.lnDisconnected.visible()
                binding.ivConnect.isEnabled = true
            }
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    private fun isVpnActive(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun isLocalVpnServiceRunning(context: Context) {
        if (!isVpnActive(context) && !LocalVpnService.IsRunning) {
            proxyViewModel.updateUI(DISCONNECTED)
            proxyViewModel.stopProxy(context)
            return
        }
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == LocalVpnService::class.java.name && LocalVpnService.IsRunning) {
                proxyViewModel.updateUI(CONNECTED)
                return
            }
        }
        if (!LocalVpnService.IsRunning) {
            proxyViewModel.updateUI(DISCONNECTED)
            proxyViewModel.stopProxy(context)
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            val start =
                context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    ?.getInt("start", 0)
            val diff = System.currentTimeMillis().toInt() / 1000 - start!!
            binding.tvTime.text = formatSecondsToTime(diff)
            handler.postDelayed(this, interval)
        }
    }

    private val speedTestRunnable = object : Runnable {
        override fun run() {
            if (BaseAppConfig.proxyHost.isNotEmpty()) {
                //update UI
                val testProxy = ProxySpeedTest.ProxyConfig(
                    host = BaseAppConfig.proxyHost,
                    port = BaseAppConfig.proxyPort.toInt(),
                    username = BaseAppConfig.proxyUser,
                    password = BaseAppConfig.proxyPass,
                    type = BaseAppConfig.proxyType
                )
                ProxySpeedTest().testProxy(testProxy,
                    callback = { download, upload ->
                        binding.tvTrafficDownload.text = download
                        binding.tvTrafficUpload.text = upload
                    }
                )
            } else {
                ProxySpeedTest().testProxy(null,
                    callback = { download, upload ->
                        binding.tvTrafficDownload.text = download
                        binding.tvTrafficUpload.text = upload
                    }
                )
            }
            handler.postDelayed(this, 10000)
        }
    }

    fun formatBytes(bytes: Int): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB", "PB")
        var value = bytes.toDouble()
        var index = 0

        while (value >= 1024 && index < units.lastIndex) {
            value /= 1024
            index++
        }

        return String.format("%.2f %s", value, units[index])
    }

    fun formatSecondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun initView() {
        super.initView()
//        isLocalVpnServiceRunning(requireContext())
        // Đăng ký BroadcastReceiver
        with(binding) {
            if (BaseAppConfig.proxyHost.isNotEmpty()) {
                //update UI
                ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
                tvProxyLocation.text = requireContext().getString(
                    context.resources.getIdentifier(
                        BaseAppConfig.proxyCountry,
                        "string",
                        requireContext().packageName
                    )
                )
                tvProxyIp.text = BaseAppConfig.proxyHost
                pnProxyInfo.visible()
                pnProxySelected.setVisible(true)
                pnSelectProxy.setVisible(false)
            } else {
                BaseAppConfig.proxyType = ""
                BaseAppConfig.proxyHost = ""
                BaseAppConfig.proxyPort = ""
                BaseAppConfig.proxyUser = ""
                BaseAppConfig.proxyPass = ""

                pnProxyInfo.invisible()
                pnProxySelected.setVisible(false)
                pnSelectProxy.setVisible(true)
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
                    handler.removeCallbacks(updateRunnable)
                    vpnPermissionLauncher.unregister()
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

    override fun onStart() {
        super.onStart()
        handler.post(updateRunnable)
        handler.post(speedTestRunnable)
        isLocalVpnServiceRunning(requireContext())
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(speedTestRunnable)
    }

    private fun startVpnService() {
        proxyViewModel.updateUI(CONNECTING)
        isConnected = true
        proxyViewModel.startProxy(context, allowApp = allowApp?.map { it.packageName })
    }

    private fun stopVpnService() {
        try {
            proxyViewModel.updateUI(DISCONNECTING)
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