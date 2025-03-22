package com.akmobile.supervpn.home

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentHomeBinding
import com.akmobile.supervpn.network.LocalVpnService

class HomeFragment : ProductFragment<FragmentHomeBinding>() {
    private var isConnected = false

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
            "CONNECTING" -> {
                binding.tvStatus.text = "Connecting..."
                binding.ivConnect.isEnabled = false
            }

            "CONNECTED" -> {
                binding.ivConnect.isSelected = true
                isConnected = true
                binding.tvStatus.text = "Connected"
                binding.ivConnect.isEnabled = true
            }

            "DISCONNECTING" -> {
                binding.tvStatus.text = "Disconnecting..."
                binding.ivConnect.isEnabled = false
            }

            "DISCONNECTED" -> {
                isConnected = false
                binding.ivConnect.isSelected = false
                binding.tvStatus.text = "Disconnected"
                binding.ivConnect.isEnabled = true
            }

            "ERROR" -> {
                binding.ivConnect.isSelected = false
                isConnected = false
                binding.tvStatus.text = "Connect (Error)"
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

        binding.ivConnect.setOnClickListener {
            if (!isConnected) {
                prepareVpn()
            } else {
                stopVpnService()
            }
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
        context?.startService(
            Intent(
                context,
                LocalVpnService::class.java
            )
        )
        binding.ivConnect.isSelected = true
        binding.tvStatus.text = "Connecting..."
    }

    private fun stopVpnService() {
        isConnected = false
        binding.ivConnect.isSelected = false
        context?.stopService( Intent(
            context,
            LocalVpnService::class.java
        ))
        binding.tvStatus.text = "Disconnecting..."
    }
}