package com.tici.vpn.proxy.master.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentDisconnectedBinding
import com.tici.vpn.proxy.master.extension.safeGetString
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.network.SpeedTest
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisconnectedFragment : BaseFragment<FragmentDisconnectedBinding>() {

    var report: SessionReport? = null


    override val screenType: ScreenType
        get() = AppScreenType.DisconnectedFragment


    companion object {
        fun newInstance(): DisconnectedFragment {
            return DisconnectedFragment()
        }
    }

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentDisconnectedBinding {
        return FragmentDisconnectedBinding.inflate(inflater, container, false)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        binding.apply {


            tvDuration.text = report?.duration
            tvUpload.text = report?.upload
            tvDownload.text = report?.download
            if (report?.upload?.isEmpty() == true && report?.upload?.isEmpty() == true) {
                SpeedTest.Instance.startSpeedTest(
                    callback = { download, upload ->
                        try {
                            if (SpeedTest.FAILED != download) tvDownload.text = download
                            if (SpeedTest.FAILED != upload) tvUpload.text = upload
                        } catch (e: Exception) {
                        }
                    })
            }
        }

        binding.tvDoneExit.setOnSingleClick {
            (activity as? MainActivity)?.navigateHome()
        }
    }

//    override fun getTitle(): String {
//        return try {
//            resources.getString(R.string.connect_report)
//        } catch (e: Exception) {
//            resources.getString(R.string.connect_report)
//        }
//    }

    fun loadData(report: SessionReport) {
        this.report = report;
    }
}