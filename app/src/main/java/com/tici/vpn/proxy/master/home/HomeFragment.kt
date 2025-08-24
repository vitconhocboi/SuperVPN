package com.tici.vpn.proxy.master.home

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.tici.vpn.proxy.master.DialogVpnPermission
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.base.ProductFragment
import com.tici.vpn.proxy.master.databinding.FragmentHomeBinding
import com.tici.vpn.proxy.master.db.VpnAppItemDB
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.proxy.ProxyViewModel
import com.tici.vpn.proxy.master.settings.appproxy.AppProxyViewModel
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.context
import com.common.baseui.extension.invisible
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.visible
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.remoteconfig.AdPlacementId
import com.simple.libads.NetworkUtils
import com.simple.libads.setVisible
import com.tici.vpn.proxy.master.guide.ViewGuide
import com.tici.vpn.proxy.master.main.SharedData
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : ProductFragment<FragmentHomeBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private var state: String? = null

    companion object {
        const val CONNECTING = "CONNECTING"
        const val CONNECTED = "CONNECTED"
        const val DISCONNECTING = "DISCONNECTING"
        const val DISCONNECTED = "DISCONNECTED"
        const val ERROR = "ERROR"
        const val INTERVAL = 10000L
    }

    private var isConnected = false
    private var proxyId: String? = ""
    private val proxyViewModel: ProxyViewModel by viewModels()

    private val allowAppViewModel: AppProxyViewModel by viewModels()

    private val mainViewModel: MainViewModel by viewModels()

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
                binding.ivConnect.setProgressWithAnimation(100f, 2000)
                binding.connectTitle.invisible()
                binding.tvTime.visible()
                binding.lnDisconnected.invisible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = true
                binding.ivConnect.isEnabled = true
//                Log.i("SuperVpn", "TestRelease isConnected true")
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
//                Log.i("SuperVpn", "TestRelease isConnected false from DISCONNECTING")
                isConnected = false
                binding.lnConnected.invisible()
            }

            DISCONNECTED -> {
                binding.ivConnect.setProgressWithAnimation(0f, 2000)
                binding.ivConnectPanel.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_round, null
                )
                binding.connectTitle.visible()
                binding.tvTime.invisible()
                binding.lnDisconnected.visible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnect.isSelected = false
                binding.ivConnect.isEnabled = true
                binding.lnConnected.invisible()
//                Log.i("SuperVpn", "TestRelease isConnected false from DISCONNECTED")
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
//                Log.i("SuperVpn", "TestRelease isConnected false from ERROR")
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
//            proxyViewModel.stopProxy(mainViewModel.getDeviceId(requireContext()), context)
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
//            proxyViewModel.stopProxy(mainViewModel.getDeviceId(requireContext()), context)
        }
    }

    var currentProxy: ProxySpeedTest.ProxyConfig? = null

//    private val speedTestRunnable = object : Runnable {
//        override fun run() {
//            if (BaseAppConfig.proxy.isNotEmpty()) {
//                ProxySpeedTest.Instance.startSpeedTest(currentProxy,
//                    callback = { download, upload ->
//                        try {
//                            if (ProxySpeedTest.FAILED != download) binding.tvTrafficDownload.text =
//                                download
//                            if (ProxySpeedTest.FAILED != upload) binding.tvTrafficUpload.text =
//                                upload
//                        } catch (e: Exception) {
//                        }
//                    })
//            }
//            handler.postDelayed(this, INTERVAL)
//        }
//    }

//    @SuppressLint("DefaultLocale")
//    fun formatBytes(bytes: Int): String {
//        if (bytes < 1024) return "$bytes B"
//        val units = arrayOf("KB", "MB", "GB", "TB", "PB")
//        var value = bytes.toDouble()
//        var index = 0
//
//        while (value >= 1024 && index < units.lastIndex) {
//            value /= 1024
//            index++
//        }
//
//        return String.format("%.2f %s", value, units[index])
//    }
//
//    @SuppressLint("DefaultLocale")
//    fun formatSecondsToTime(seconds: Int): String {
//        val hours = seconds / 3600
//        val minutes = (seconds % 3600) / 60
//        val secs = seconds % 60
//        return String.format("%02d:%02d:%02d", hours, minutes, secs)
//    }

    fun Context.safeGetString(resourceName: String): String? {
        val resId = resources.getIdentifier(resourceName, "string", packageName)
        return if (resId != 0) getString(resId) else null
    }

    override fun initView() {
        super.initView()
        state = activity?.intent?.getStringExtra("RECONNECT")
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
//        isLocalVpnServiceRunning(requireContext())
        with(binding) {
            if (BaseAppConfig.isCanShowGuide) {
                ViewGuide().show(parentFragmentManager, "dialog_guide")
            }
            ivSelectProxy.scaleX = if (isRTL) -1f else 1f

            allowAppViewModel.getAllowApp()
            Timber.d("Test_Subscribe allowAppViewModel")
            bindFlowCreate(allowAppViewModel.allowApp) { result ->
                processResultData(result, onSuccess = {
                    allowApp = it
                })
            }

            proxyViewModel.currentProxy.observe(viewLifecycleOwner) {

            }

            ivConnect.apply {
                progress = 0f
                progressMax = 100f
                roundBorder = true
                startAngle = 0f

                onProgressChangeListener = { progress ->
                    if (progress == 100f) {
                        binding.ivConnectPanel.background = ResourcesCompat.getDrawable(
                            resources, R.drawable.bg_round_active, null
                        )
                    } else if (progress == 0f) {
                        binding.ivConnectPanel.background = ResourcesCompat.getDrawable(
                            resources, R.drawable.bg_round, null
                        )
                    }
                }
            }

            ivConnect.setOnClickListener {
                if (!isConnected) {
                    if (BaseAppConfig.proxy.isNotEmpty()) {
                        prepareVpn()
                    } else {
                        ConfirmDnsDialog(requireActivity(), onContinue = {
                            prepareVpn()
                        }, selectVpn = {
                            Navigator.startProxyActivity(requireContext(), null)
                        }).show()
                    }
                } else {
                    if (NetworkUtils.isInternetAvailable(requireActivity())) {
                        stopSpeedTest()
                        vpnPermissionLauncher.unregister()
                        stopVpnService()
                    } else {
                        Toast.makeText(
                            requireContext(), "No internet connection", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

//            proxyViewModel.currentProxy.observe(viewLifecycleOwner) { it ->
//                if (it != null) {
//                    tvProxyIp.setVisible(true)
//                    tvProxyIp.text = it.host
//                    val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
//                    tvProxyLocation.typeface = typeface
//                    tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
//                }
//            }

            pnSelectProxy.setOnClickListener {
                showInterAds(placementId = AdPlacementId.INTER_ART_HOME,
                    impressedCallBack = {},
                    showCallBack = {
                        Navigator.startProxyActivity(requireContext(), null)
                    })
            }

//<<<<<<< HEAD
            if (BaseAppConfig.proxy.isNotEmpty()) {
                ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
                tvProxyLocation.text = requireContext().safeGetString(BaseAppConfig.proxyCountry)
                engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
                    if (parts.size >= 2) {
                        BaseAppConfig.proxyHost = parts[1]
                        currentProxy = ProxySpeedTest.ProxyConfig(
                            host = parts[1],
                            port = parts[2].toInt(),
                            username = parts.getOrNull(3) ?: "",
                            password = parts.getOrNull(4) ?: "",
                            type = parts.getOrNull(0) ?: "http"
                        )
//=======
            pnIpInfo.setOnClickListener {
                Navigator.startProxyInfoActivity(requireContext(), currentProxy)
            }

//            SharedData.isSub.observe(viewLifecycleOwner) { it ->
////                Log.i("SuperVpn", "TestRelease isSub home_fragment $it")
//                if (it == false) {
////                    Log.i("SuperVpn", "TestRelease isConnected $isConnected")
//                    if (isConnected) {
////                        Log.i("SuperVpn", "TestRelease BaseAppConfig.proxy ${BaseAppConfig.proxy} ${BaseAppConfig.proxy.isNotEmpty()}")
//                        if (BaseAppConfig.proxy.isNotEmpty()) { //neu chi dung dns thi giu nguyen
//                            if (NetworkUtils.isInternetAvailable(requireActivity())) {
////                                Log.i("SuperVpn", "TestRelease has internet and stop")
//                                stopSpeedTest()
//                                vpnPermissionLauncher.unregister()
////                                Log.i("SuperVpn", "TestRelease has stopVpnService and stop")
//                                stopVpnService()
//                            }
//                        }
//                    }
//                    with(binding) {
////                        Log.i("SuperVpn", "TestRelease BaseAppConfig.proxy ${BaseAppConfig.proxy}")
//                        if (BaseAppConfig.proxy.isNotEmpty()) {
//                            ivConnect.isSelected = false
////                            Log.i("SuperVpn", "TestRelease BaseAppConfig.proxy isConnected ${isConnected}")
//                            if (!isConnected) {
//                                BaseAppConfig.proxy = ""
//                                BaseAppConfig.proxyHost = ""
//                                BaseAppConfig.proxyCountry = ""
//                            }
//                        }
//                        ivFlag.setImageResource(R.drawable.ic_earth)
////                        BaseAppConfig.proxy = ""
////                        BaseAppConfig.proxyHost = ""
////                        BaseAppConfig.proxyCountry = ""
//                        tvProxyLocation.text = getString(R.string.ip_proxy)
//                        tvProxyIp.setVisible(false)
//                        val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
//                        tvProxyLocation.typeface = typeface
//                        tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
//                        currentProxy = null
//                    }
//                } else {
////                    Log.i("SuperVpn", "TestRelease proxy ${BaseAppConfig.proxy}")
//                    if (BaseAppConfig.proxy.isNotEmpty()) {
//                        //update UI
//                        ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
//                        tvProxyLocation.text =
//                            requireContext().safeGetString(BaseAppConfig.proxyCountry)
//                        engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
//                            if (parts.size >= 2) {
//                                BaseAppConfig.proxyHost = parts[1]
//                                currentProxy = ProxySpeedTest.ProxyConfig(
//                                    host = parts[1],
//                                    port = parts[2].toInt(),
//                                    username = parts.getOrNull(3) ?: "",
//                                    password = parts.getOrNull(4) ?: "",
//                                    type = parts.getOrNull(0) ?: "http"
//                                )
//                            } else {
//                                currentProxy = null
//                            }
//                        }
//
//                        tvProxyIp.setVisible(true)
////                        Log.i("SuperVpn", "TestRelease update text ${currentProxy?.host}")
//                        tvProxyIp.text = currentProxy?.host
//                        val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
//                        tvProxyLocation.typeface = typeface
//                        tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
//                    } else {
//                        currentProxy = null
//                    }
//                }
//
//                tvProxyIp.setVisible(true)
//                tvProxyIp.text = currentProxy?.host
//                val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
//                tvProxyLocation.typeface = typeface
//                tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
//            } else {
                ivFlag.setImageResource(R.drawable.ic_earth)
                BaseAppConfig.proxy = ""
                BaseAppConfig.proxyHost = ""
                BaseAppConfig.proxyCountry = ""
                tvProxyLocation.text = getString(R.string.ip_proxy)
                tvProxyIp.setVisible(false)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
                tvProxyLocation.typeface = typeface
                tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
                currentProxy = null
            }

//            SharedData.isSub.observe(viewLifecycleOwner) { it ->
//                if (it == false) {
//                    if (isConnected) {
//                        if (BaseAppConfig.proxy.isNotEmpty()) { //neu chi dung dns thi giu nguyen
//                            if (NetworkUtils.isInternetAvailable(requireActivity())) {
//                                stopSpeedTest()
//                                vpnPermissionLauncher.unregister()
//                                stopVpnService()
//                            }
//                        }
//                    }
//                    with (binding) {
//                        if (BaseAppConfig.proxy.isNotEmpty()) {
//                            ivConnect.isSelected = false
//                            if (!isConnected) {
//                                BaseAppConfig.proxy = ""
//                                BaseAppConfig.proxyHost = ""
//                                BaseAppConfig.proxyCountry = ""
//                            }
//                        }
//                        ivFlag.setImageResource(R.drawable.ic_earth)
//                        tvProxyLocation.text = getString(R.string.ip_proxy)
//                        tvProxyIp.setVisible(false)
//                        val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
//                        tvProxyLocation.typeface = typeface
//                        tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
//                        currentProxy = null
//                    }
//                } else {
//                    if (BaseAppConfig.proxy.isNotEmpty()) {
//                        ivFlag.setImageResource(Utils.getFlag(BaseAppConfig.proxyCountry))
//                        tvProxyLocation.text = requireContext().safeGetString(BaseAppConfig.proxyCountry)
//                        engine.Engine.decodeString(BaseAppConfig.proxy).split(":").let { parts ->
//                            if (parts.size >= 2) {
//                                BaseAppConfig.proxyHost = parts[1]
//                                currentProxy = ProxySpeedTest.ProxyConfig(
//                                    host = parts[1],
//                                    port = parts[2].toInt(),
//                                    username = parts.getOrNull(3) ?: "",
//                                    password = parts.getOrNull(4) ?: "",
//                                    type = parts.getOrNull(0) ?: "http"
//                                )
//                            } else {
//                                currentProxy = null
//                            }
//                        }
//
//                        tvProxyIp.setVisible(true)
//                        tvProxyIp.text = currentProxy?.host
//                        val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
//                        tvProxyLocation.typeface = typeface
//                        tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
//                    } else {
//                        ivFlag.setImageResource(R.drawable.ic_earth)
//                        BaseAppConfig.proxy = ""
//                        BaseAppConfig.proxyHost = ""
//                        BaseAppConfig.proxyCountry = ""
//                        tvProxyLocation.text = getString(R.string.ip_proxy)
//                        tvProxyIp.setVisible(false)
//                        val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
//                        tvProxyLocation.typeface = typeface
//                        tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
//                        currentProxy = null
//                    }
//                }
//            }

            proxyId = arguments?.getString("id", null)
        }

        proxyViewModel.isConnected.observe(viewLifecycleOwner) { status ->
            updateUI(status)
        }

        proxyViewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                Toast.makeText(
                    requireContext(), "Connection error. Please try again.", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun onToggleStop() {
        if (!isConnected) {
            if (BaseAppConfig.proxy.isNotEmpty()) {
                prepareVpn()
            } else {
                ConfirmDnsDialog(requireActivity(), onContinue = {
                    prepareVpn()
                }, selectVpn = {
                    Navigator.startProxyActivity(requireContext(), null)
                }).show()
            }
        } else {
            if (NetworkUtils.isInternetAvailable(requireActivity())) {
                stopSpeedTest()
                vpnPermissionLauncher.unregister()
                stopVpnService()
            } else {
                Toast.makeText(
                    requireContext(), "No internet connection", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun prepareVpn() {
        if (NetworkUtils.isInternetAvailable(requireActivity())) {
            val intent = VpnService.prepare(context)
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                startVpnService()
//                handler.post(speedTestRunnable)
            }
        } else {
            Toast.makeText(
                requireContext(), "No internet connection", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (LocalVpnService.IsRunning) {
//            handler.post(speedTestRunnable)
        }
        isLocalVpnServiceRunning(requireContext())
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
//        handler.removeCallbacks(speedTestRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        handler.removeCallbacks(speedTestRunnable)
    }

    fun stopSpeedTest() {
//        binding.tvTrafficDownload.text = "--"
//        binding.tvTrafficUpload.text = "--"
//        handler.removeCallbacks(speedTestRunnable)
        ProxySpeedTest.Instance.stopProxyTest()
    }

    private fun startVpnService() {
        proxyViewModel.updateUI(CONNECTING)
        isConnected = true
        allowAppViewModel.getAllowApp()
        bindFlowCreate(allowAppViewModel.allowApp) { result ->
            processResultData(result, onSuccess = { rs ->
                try {
                    binding.ivConnect.setProgressWithAnimation(99f, 10000)
                    proxyViewModel.startProxy(mainViewModel.getDeviceId(requireContext()),
                        context,
                        allowApp = rs.map { it.packageName })
                } catch (_: Exception) {
                    Toast.makeText(
                        requireContext(), "Network error. Please try again", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }

    private fun stopVpnService() {
        try {
            proxyViewModel.updateUI(DISCONNECTING)
            proxyViewModel.stopProxy(mainViewModel.getDeviceId(requireContext()), context)
//            binding.tvTrafficDownload.text = "--"
//            binding.tvTrafficUpload.text = "--"
        } catch (e: Exception) {
            Timber.e(e, "VPN Error stopping VPN service")
        }
    }
}
