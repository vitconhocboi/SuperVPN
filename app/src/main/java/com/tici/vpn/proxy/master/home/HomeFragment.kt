package com.tici.vpn.proxy.master.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.bindFlowCreate
import com.common.baseui.extension.context
import com.common.baseui.extension.invisible
import com.common.baseui.extension.processResultData
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.common.baseui.extension.visible
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.IAdPlaceName
import com.simple.libads.NetworkUtils
import com.simple.libads.setVisible
import com.tici.vpn.proxy.master.DialogVpnPermission
import com.tici.vpn.proxy.master.DisconnectConfirmDialog
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentHomeBinding
import com.tici.vpn.proxy.master.db.VpnAppItemDB
import com.tici.vpn.proxy.master.extension.safeGetString
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.ProxySpeedTest
import com.tici.vpn.proxy.master.proxy.ProxyViewModel
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.settings.appproxy.AppProxyViewModel
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {


    override val screenType: ScreenType
        get() = AppScreenType.HomeFragment

    private val handler = Handler(Looper.getMainLooper())
    private var state: String? = null

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.FULLSCREEN_SELECTED_PROXY_HOME
        )
    }

    companion object {
        const val CONNECTING = "CONNECTING"
        const val CONNECTED = "CONNECTED"
        const val DISCONNECTING = "DISCONNECTING"
        const val DISCONNECTED = "DISCONNECTED"
        const val ERROR = "ERROR"
        const val INTERVAL = 10000L
        var isShowReport = true
        var lastProcess = 50f
        var report = ProxyReport()
        var isShowDisconnect = true

        fun newInstance(
            showConnected: (proxy: ProxySpeedTest.ProxyConfig?) -> Unit,
            showDisconnected: (report: ProxyReport) -> Unit
        ): HomeFragment {
            val fragment = HomeFragment()
            /*fragment.onShowConnected = showConnected
            fragment.onShowDisconnected = showDisconnected*/
            return fragment
        }
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
                isShowReport = false
                startVpnService()
            } else {
                isShowReport = true
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
                binding.ivConnectPanel.isEnabled = false
                binding.lnConnected.invisible()
            }

            CONNECTED -> {
                if (state == "RECONNECT") {
                    state = "CONNECT"
                    stopVpnService()
                } else if (!NetworkUtils.isInternetAvailable(requireContext())) {
                    state = ""
                    stopVpnService()
                } else {
//                    binding.ivConnect.setProgressWithAnimation(100f, 2000)
                    binding.ivConnectPanel.background = ResourcesCompat.getDrawable(
                        resources, R.drawable.bg_round_active, null
                    )
                    binding.connectTitle.invisible()
                    binding.tvTime.visible()
                    binding.lnDisconnected.invisible()
                    binding.lnDisconnecting.invisible()
                    binding.lnConnecting.invisible()
                    binding.ivConnectPanel.isSelected = true
                    binding.ivConnectPanel.isEnabled = true
                    isConnected = true
                    report.proxyConfig = currentProxy
                    binding.lnConnected.visible()
                    if (!isShowReport) {
                        isShowReport = true
                        (activity as? MainActivity)?.showConnected(currentProxy)
                    }
                }
            }

            DISCONNECTING -> {
                binding.connectTitle.invisible()
                binding.tvTime.visible()
                binding.lnDisconnected.invisible()
                binding.lnDisconnecting.visible()
                binding.lnConnecting.invisible()
                binding.ivConnectPanel.isSelected = false
                binding.ivConnectPanel.isEnabled = false
//                Log.i("SuperVpn", "TestRelease isConnected false from DISCONNECTING")
                isConnected = false
                binding.lnConnected.invisible()
            }

            DISCONNECTED -> {
//                binding.ivConnect.setProgressWithAnimation(0f, 2000)
                binding.ivConnectPanel.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.bg_round, null
                )
                binding.connectTitle.visible()
                binding.tvTime.invisible()
                binding.lnDisconnected.visible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnectPanel.isSelected = false
                binding.ivConnectPanel.isEnabled = true
                binding.lnConnected.invisible()
                isConnected = false
                if (state != "CONNECT" && !isShowReport) {
                    isShowReport = true
                    val start = context?.getSharedPreferences(
                        "privoxy_traffic", Context.MODE_PRIVATE
                    )?.getInt("start", 0)
                    val diff = System.currentTimeMillis().toInt() / 1000 - start!!
                    report.duration = formatSecondsToTime(diff)
                    (activity as? MainActivity)?.showDisconnected(report)
                }
                if (state == "CONNECT") {
                    isShowReport = false
                    state = ""
                    prepareVpn()
                    handler.post(speedTestRunnable)
                    handler.post(updateRunnable)
                }
            }

            ERROR -> {
                binding.connectTitle.visible()
                binding.tvTime.invisible()
                binding.lnDisconnected.visible()
                binding.lnDisconnecting.invisible()
                binding.lnConnecting.invisible()
                binding.ivConnectPanel.isSelected = false
                binding.ivConnectPanel.isEnabled = false
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

    private val speedTestRunnable = object : Runnable {
        override fun run() {
            if (BaseAppConfig.proxy.isNotEmpty()) {
                ProxySpeedTest.Instance.startSpeedTest(
                    currentProxy,
                    callback = { download, upload ->
                        try {
                            if (download.isNotEmpty()) report.download = download
                            if (upload.isNotEmpty()) report.upload = upload
                        } catch (e: Exception) {
                        }
                    })
            }
            handler.postDelayed(this, INTERVAL)
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            val start = context?.getSharedPreferences("privoxy_traffic", Context.MODE_PRIVATE)
                ?.getInt("start", 0)
            val diff = System.currentTimeMillis().toInt() / 1000 - start!!
            binding.tvPrivateInternet.text = formatSecondsToTime(diff)
            handler.postDelayed(this, 1000L)
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatSecondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        state = activity?.intent?.getStringExtra("state")
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        with(binding) {
            ivSelectProxy.scaleX = if (isRTL) -1f else 1f
            ivIpInfo.scaleX = if (isRTL) -1f else 1f

            allowAppViewModel.getAllowApp()
            bindFlowCreate(allowAppViewModel.allowApp) { result ->
                processResultData(result, onSuccess = {
                    allowApp = it
                })
            }

            proxyViewModel.currentProxy.observe(viewLifecycleOwner) {

            }

            ivConnectPanel.setOnClickListener {
                if (NetworkUtils.isInternetAvailable(requireActivity())) {
                    if (isConnected) {
                        showInterAd(AppAdPlaceName.FULLSCREEN_SELECTED_PROXY_HOME) {
                            isShowDisconnect = false
                            actionConnectProxy()
                        }
                    } else {
                        isShowDisconnect = false
                        actionConnectProxy()
                    }
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            pnSelectProxy.setOnClickListener {
                if (NetworkUtils.isInternetAvailable(requireActivity())) {
                    Navigator.startProxyActivity(requireContext(), null)
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            pnIpInfo.setOnClickNoDoubleClick {
                if (NetworkUtils.isInternetAvailable(requireActivity())) {
                    Navigator.startProxyInfoActivity(requireContext(), currentProxy)
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (BaseAppConfig.proxy.isNotEmpty()) {
                showIpCountry()
            } else {
                ivFlag.setImageResource(R.drawable.ic_earth)
                BaseAppConfig.proxy = ""
                BaseAppConfig.proxyHost = ""
                BaseAppConfig.proxyCountry = ""
                BaseAppConfig.proxyGroup = ""
                tvProxyLocation.text = getString(R.string.ip_proxy)
                tvProxyIp.setVisible(false)
                val typeface = ResourcesCompat.getFont(context, R.font.inter_normal)
                tvProxyLocation.typeface = typeface
                tvProxyLocation.setTextColor(resources.getColor(R.color.green_1))
                currentProxy = null
            }

            proxyId = arguments?.getString("id", null)
        }

        proxyViewModel.isConnected.observe(viewLifecycleOwner) { status ->

            updateUI(status)
        }

        proxyViewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                Toast.makeText(
                    requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT
                ).show()
            }
        }

        proxyViewModel.freeProxy.observe(viewLifecycleOwner) {
            if (it != null && it.country.isNotEmpty()) {
                lifecycleScope.launch {
                    val deviceId = mainViewModel.getDeviceId(requireContext())
                    proxyViewModel.setActiveProxy(it, deviceId, "free")
                    prepareVpn()
                    showIpCountry()
                }
            } else if (it.country.isEmpty()) {
                Toast.makeText(
                    requireContext(), getString(R.string.no_available_vpn), Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun actionConnectProxy() {
        if (!isConnected) {
            if (NetworkUtils.isInternetAvailable(requireActivity())) {
                isShowReport = false
                if (BaseAppConfig.proxy.isNotEmpty()) {
                    prepareVpn()
                } else {
                    lifecycleScope.launch {
                        proxyViewModel.getRandomFreeProxy()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            DisconnectConfirmDialog(onConfirm = {
                isShowReport = false
                if (NetworkUtils.isInternetAvailable(requireActivity())) {
                    stopSpeedTest()
                    vpnPermissionLauncher.unregister()
                    stopVpnService()
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                    ).show()
                }
            }).show(childFragmentManager, "DialogConfirm")
        }
    }

    private fun showInterAdsFullSelectedProxy() {
        showInterAd(AppAdPlaceName.FULLSCREEN_SELECTED_PROXY_HOME) {
            if (isAdded && view != null)
                Navigator.startProxyActivity(requireContext(), null)
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
                    requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showIpCountry() {
        with(binding) {
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
                } else {
                    currentProxy = null
                }
            }

            tvProxyIp.setVisible(true)
            tvProxyIp.text = currentProxy?.host
            val typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
            tvProxyLocation.typeface = typeface
            tvProxyLocation.setTextColor(resources.getColor(R.color.language_item_text_color))
        }
    }

    private fun prepareVpn() {
        if (NetworkUtils.isInternetAvailable(requireActivity())) {
            val intent = VpnService.prepare(context)
            if (intent != null) {
                isShowReport = true
                vpnPermissionLauncher.launch(intent)
            } else {
                startVpnService()
                handler.post(speedTestRunnable)
                handler.post(updateRunnable)
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (LocalVpnService.IsRunning) {
            handler.post(speedTestRunnable)
            handler.post(updateRunnable)
        }
        isLocalVpnServiceRunning(requireContext())
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(speedTestRunnable)
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(speedTestRunnable)
        handler.removeCallbacks(updateRunnable)
    }

    private fun stopSpeedTest() {
        handler.removeCallbacks(speedTestRunnable)
        handler.removeCallbacks(updateRunnable)
        ProxySpeedTest.Instance.stopProxyTest()
    }

    private fun startVpnService() {
        proxyViewModel.updateUI(CONNECTING)
        isConnected = true
        allowAppViewModel.getAllowApp()
        bindFlowCreate(allowAppViewModel.allowApp) { result ->
            processResultData(result, onSuccess = { rs ->
                try {
                    proxyViewModel.startProxy(
                        mainViewModel.getDeviceId(requireContext()),
                        context,
                        allowApp = rs.map { it.packageName })
                } catch (_: Exception) {
                    Toast.makeText(
                        requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }

    private fun stopVpnService() {
        try {
            proxyViewModel.updateUI(DISCONNECTING)
            proxyViewModel.stopProxy(mainViewModel.getDeviceId(requireContext()), context)
        } catch (e: Exception) {
            Timber.e(e, "VPN Error stopping VPN service")
        }
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(
            adResource,
            AppAdPlaceName.ANCHORED_CENTER_HOME
        )
    }

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.ANCHORED_CENTER_HOME
        )
    }
}