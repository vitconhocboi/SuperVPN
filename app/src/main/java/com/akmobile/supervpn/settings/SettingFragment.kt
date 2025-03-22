package com.akmobile.supervpn.settings

import android.app.Activity
import android.net.VpnService
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentSettingBinding

class SettingFragment : ProductFragment<FragmentSettingBinding>() {
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

    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()

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
//        isConnected = true
//        context?.startService(
//            Intent(
//                context,
//                LocalVpnService::class.java
//            )
//        )
    }

    private fun stopVpnService() {
//        isConnected = false
//        binding.ivConnect.isSelected = false
//        context?.stopService( Intent(
//            context,
//            LocalVpnService::class.java
//        ))
//        binding.tvStatus.text = "Disconnecting..."
    }
}