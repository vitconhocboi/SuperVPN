package com.tici.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.common.baseui.extension.setVisible
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.tici.vpn.proxy.master.databinding.FragmentPremiumBinding
import com.tici.vpn.proxy.master.main.MainViewModel
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumFragment : BaseFragment<FragmentPremiumBinding>() {

    private val premiumViewModel: PremiumViewModel by viewModels()

    private val mainViewModel: MainViewModel by viewModels()

    private val skuAdapter = SkuAdapter()

    private var selectedSku = Sku()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.PremiumFragment


    @SuppressLint("NotifyDataSetChanged")
    override fun initViews(savedInstanceState: Bundle?) {

        premiumViewModel.loadData()

        skuAdapter.onItemClick = { pos, item ->
//            Timber.d("item $pos ${item.productName} is selected")
            selectedSku = item
            skuAdapter.notifyDataSetChanged()
        }

        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        binding.apply {

            frameCorner.scaleX = if (isRTL) -1f else 1f

            rcvSkus.adapter = skuAdapter

            btnClose.setOnClickListener {
                (activity as? PremiumActivity)?.apply {
                    finish()
                }
            }

            startTrial.setOnClickListener {
//                if (!BaseAppConfig.allowCollectData) {
//                    AcceptCollectDataDialog(
//                        requireActivity(),
//                        onContinue = {
//                            BaseAppConfig.allowCollectData = true
//                            startTrial()
//                        }
//                    ).show()
//                } else {
                startTrial()
//                }
            }
        }

        premiumViewModel.skus.observe(viewLifecycleOwner) { it ->
            if (it.isNotEmpty()) {
                skuAdapter.updateData(it)
            } else {
                Toast.makeText(
                    requireContext(),
                    "System error. Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        premiumViewModel.isLoading.observe(this) { it ->
            if (it == true) {
                binding.progress.setVisible(true)
            } else {
                binding.progress.setVisible(false)
            }
        }

        premiumViewModel.sub.observe(this) { it ->
            if (it == true) {
                (activity as? PremiumActivity)?.apply {
                    finish()
                }
            }
        }

//        SharedData.isSub.observe(viewLifecycleOwner) { it ->
//            if (it == true) {
//                Timber.d("subs successfully")
//            }
//        }
    }

    private fun startTrial() {
        if (selectedSku.productName.isNotEmpty()) {
            mainViewModel.getDeviceId(requireContext())
            premiumViewModel.launchSubscription(
                requireActivity(),
                selectedSku.productName
            )
        } else {
            Toast.makeText(
                requireContext(),
                com.tici.vpn.proxy.master.R.string.premium_select_sku,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}