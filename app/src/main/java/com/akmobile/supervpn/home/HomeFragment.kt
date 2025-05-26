package com.akmobile.supervpn.home

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.akmobile.supervpn.DialogVpnPermission

import com.akmobile.supervpn.base.ProductFragment
import com.akmobile.supervpn.databinding.FragmentHomeBinding
import com.akmobile.supervpn.network.LocalVpnService
import com.akmobile.supervpn.proxy.ProxyViewModel
import com.akmobile.supervpn.utils.Navigator
import com.akmobile.supervpn.utils.Utils
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.invisible
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import com.akmobile.supervpn.R
import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.network.ProxySpeedTest
import com.akmobile.supervpn.settings.appproxy.AppProxyViewModel
import com.common.baseui.extension.context
import com.simple.libads.NetworkUtils
import com.simple.libads.setVisible

@AndroidEntryPoint
class HomeFragment : ProductFragment<FragmentHomeBinding>() {
    private val handler = Handler(Looper.getMainLooper())
    private var state: String? = null

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
            } else {
                DialogVpnPermission().show(
                    requireActivity().supportFragmentManager, "DialogVpnPermission"
                )
            }
        }

    private fun updateUI(status: String?) {
        when (status) {
            CONNECTING -> {
                binding.connectTitle.invisible()
                binding.tvTime.visible()
                binding.lnDisconnecting.invisible()
                binding.lnDisconnected.invisible()
                binding.lnConnecting.visible()
                binding.ivConnect.isEnabled = false
                binding.lnConnected.invisible()
            }

            CONNECTED -> {
                binding.connectTitle.invisible()
                binding.tvTime.visible()
                binding.lnDisconnected.invisible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = true
                binding.ivConnect.isEnabled = true
                isConnected = true
                binding.lnConnected.visible()
                if (state == "RECONNECT") {
                    state = "CONNECT"
                    stopVpnService()
                }
            }

            DISCONNECTING -> {
                binding.connectTitle.invisible()
                binding.tvTime.visible()
                binding.lnDisconnected.invisible()
                binding.lnDisconnecting.visible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = false
                binding.ivConnect.isEnabled = false
                isConnected = false
                binding.lnConnected.invisible()
            }

            DISCONNECTED -> {
                binding.connectTitle.visible()
                binding.tvTime.invisible()
                binding.lnDisconnected.visible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = false
                binding.ivConnect.isEnabled = true
                binding.lnConnected.invisible()
                isConnected = false
                if (state == "CONNECT") {
                    state = ""
                    startVpnService()
                }
            }

            ERROR -> {
                binding.connectTitle.visible()
                binding.tvTime.invisible()
                binding.lnDisconnected.visible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = false
                binding.ivConnect.isEnabled = false
                binding.lnConnected.invisible()
                isConnected = false
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

//    private val updateRunnable = object : Runnable {
//        override fun run() {
//            val start = context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
//                ?.getInt("start", 0)
//            val diff = System.currentTimeMillis().toInt() / 1000 - start!!
//            binding.tvTime.text = formatSecondsToTime(diff)
//            handler.postDelayed(this, interval)
//        }
//    }

    var currentProxy: ProxySpeedTest.ProxyConfig? = null

    private val speedTestRunnable = object : Runnable {
        override fun run() {
            if (BaseAppConfig.proxy.isNotEmpty()) {
                //update UI
//                val testProxy = ProxySpeedTest.ProxyConfig(
//                    host = BaseAppConfig.proxyHost,
//                    port = BaseAppConfig.proxyPort.toInt(),
//                    username = BaseAppConfig.proxyUser,
//                    password = BaseAppConfig.proxyPass,
//                    type = BaseAppConfig.proxyType
//                )
                ProxySpeedTest().testProxy(currentProxy, callback = { download, upload ->
                    binding.tvTrafficDownload.text = download
                    binding.tvTrafficUpload.text = upload
                })
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
        state = activity?.intent?.getStringExtra("RECONNECT")
//        isLocalVpnServiceRunning(requireContext())
        // Đăng ký BroadcastReceiver
        with(binding) {
            if (BaseAppConfig.proxy.isNotEmpty()) {
                //update UI
                ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
                tvProxyLocation.text = requireContext().getString(
                    context.resources.getIdentifier(
                        BaseAppConfig.proxyCountry, "string", requireContext().packageName
                    )
                )
                engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
                    if (parts.size >= 2) {
                        currentProxy = ProxySpeedTest.ProxyConfig(
                            host = parts[1],
                            port = parts[2].toInt(),
                            username = parts.getOrNull(3) ?: "",
                            password = parts.getOrNull(4) ?: "",
                            type = parts.getOrNull(0) ?: "http"
                        )
                    } else {
                        currentProxy = null
                    }
                }

                tvProxyIp.setVisible(true)
                tvProxyIp.text = currentProxy?.host
                val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                tvProxyLocation.typeface = typeface
                tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
            } else {
                ivFlag.setImageResource(R.drawable.ic_earth)
                BaseAppConfig.proxy = ""
                tvProxyLocation.text = getString(R.string.ip_proxy)
                tvProxyIp.setVisible(false)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
                tvProxyLocation.typeface = typeface
                tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
                currentProxy = null
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
//                    handler.removeCallbacks(updateRunnable)
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
        if (NetworkUtils.isInternetAvailable(requireActivity())) {
            val intent = VpnService.prepare(context)
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                startVpnService()
            }
        } else {
            Toast.makeText(
                requireContext(), "No internet connection", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
//        handler.post(updateRunnable)
        handler.post(speedTestRunnable)
        isLocalVpnServiceRunning(requireContext())
    }

    override fun onStop() {
        super.onStop()
//        handler.removeCallbacks(updateRunnable)
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
            binding.tvTrafficDownload.text = "--"
            binding.tvTrafficUpload.text = "--"
//            LocalVpnService.IsRunning = false
//            isConnected = false
//            binding.ivConnect.isSelected = false
//            binding.tvStatus.text = activity?.resources?.getString(R.string.disconnecting)
        } catch (e: Exception) {
            Log.e("VPN", "Error stopping VPN service", e)
        }
    }
}