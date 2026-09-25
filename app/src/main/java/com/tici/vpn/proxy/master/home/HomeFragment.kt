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
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.tici.vpn.proxy.master.utils.NetworkUtils
import com.common.baseui.extension.setVisible
import com.tici.vpn.proxy.master.DialogVpnPermission
import com.tici.vpn.proxy.master.DisconnectConfirmDialog
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentHomeBinding
import com.tici.vpn.proxy.master.db.VpnAppItemDB
import com.tici.vpn.proxy.master.extension.safeGetString
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.network.LocalVpnService
import com.tici.vpn.proxy.master.network.SpeedTest
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

    companion object {
        const val CONNECTING = "CONNECTING"
        const val CONNECTED = "CONNECTED"
        const val DISCONNECTING = "DISCONNECTING"
        const val DISCONNECTED = "DISCONNECTED"
        const val ERROR = "ERROR"
        const val INTERVAL = 10000L
        var isShowReport = true
        var lastProcess = 50f
        var report = SessionReport()
        var isShowDisconnect = true

        fun newInstance(
            showConnected: () -> Unit,
            showDisconnected: (report: SessionReport) -> Unit
        ): HomeFragment {
            val fragment = HomeFragment()
            /*fragment.onShowConnected = showConnected
            fragment.onShowDisconnected = showDisconnected*/
            return fragment
        }
    }

    private var isConnected = false
    private val vpnViewModel: VpnConnectionViewModel by viewModels()

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
                    binding.lnConnected.visible()
                    if (!isShowReport) {
                        isShowReport = true
                        (activity as? MainActivity)?.showConnected()
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
            vpnViewModel.updateUI(DISCONNECTED)
//            vpnViewModel.stopVpn(context)
            return
        }
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == LocalVpnService::class.java.name && LocalVpnService.IsRunning) {
                vpnViewModel.updateUI(CONNECTED)
                return
            }
        }
        if (!LocalVpnService.IsRunning) {
            vpnViewModel.updateUI(DISCONNECTED)
//            vpnViewModel.stopVpn(context)
        }
    }

    private val speedTestRunnable = object : Runnable {
        override fun run() {
            run {
                SpeedTest.Instance.startSpeedTest(
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
            allowAppViewModel.getAllowApp()
            bindFlowCreate(allowAppViewModel.allowApp) { result ->
                processResultData(result, onSuccess = {
                    allowApp = it
                })
            }

            ivConnectPanel.setOnClickListener {
                isShowDisconnect = false
                // actionConnectProxy() does its own connectivity check for the connect path,
                // and disconnecting must stay possible even without internet.
                actionConnectProxy()
            }

        }

        vpnViewModel.isConnected.observe(viewLifecycleOwner) { status ->

            updateUI(status)
        }

    }

    private fun actionConnectProxy() {
        if (!isConnected) {
            if (NetworkUtils.isInternetAvailable(requireActivity())) {
                isShowReport = false
                prepareVpn()
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            DisconnectConfirmDialog(onConfirm = {
                isShowReport = false
                // Never gate disconnect on connectivity: the user would be stuck connected.
                stopSpeedTest()
                vpnPermissionLauncher.unregister()
                stopVpnService()
            }).show(childFragmentManager, "DialogConfirm")
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
        SpeedTest.Instance.stopProxyTest()
    }

    private fun startVpnService() {
        vpnViewModel.updateUI(CONNECTING)
        isConnected = true
        allowAppViewModel.getAllowApp()
        bindFlowCreate(allowAppViewModel.allowApp) { result ->
            processResultData(result, onSuccess = { rs ->
                try {
                    vpnViewModel.startVpn(context, allowApp = rs.map { it.packageName })
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
            vpnViewModel.updateUI(DISCONNECTING)
            vpnViewModel.stopVpn(context)
        } catch (e: Exception) {
            Timber.e(e, "VPN Error stopping VPN service")
        }
    }

}