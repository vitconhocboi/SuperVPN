package com.akmobile.supervpn.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Looper
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

//    private val vpnStatusReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val status = intent?.getStringExtra(SupperVpnService.EXTRA_VPN_STATUS)
//            updateUI(status)
//        }
//    }

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

    private val updateRunnable = object : Runnable {
        override fun run() {
            val trafficDownload =
                context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    ?.getInt("download", 0)
            if (trafficDownload != null && trafficDownload > 0) {
                binding.tvTrafficDownload.text = formatBytes(trafficDownload)
            } else {
                binding.tvTrafficDownload.text = "--"
            }

            val trafficUpload =
                context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    ?.getInt("upload", 0)
            if (trafficUpload != null && trafficUpload > 0) {
                binding.tvTrafficUpload.text = formatBytes(trafficUpload)
            } else {
                binding.tvTrafficUpload.text = "--"
            }
            val start =
                context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                    ?.getInt("start", 0)
            val diff = System.currentTimeMillis().toInt() / 1000 - start!!

            binding.tvTime.text = formatSecondsToTime(diff)
            handler.postDelayed(this, interval)
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

            tvTrafficDownload.text =
                context?.getSharedPreferences("PRIVOXY_TRAFFIC", Context.MODE_PRIVATE)
                    ?.getInt("DOWNLOAD", 0).toString()

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
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
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