package com.highsecure.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.setVisible
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.FragmentPremiumBinding
import com.highsecure.vpn.proxy.master.main.MainViewModel
import com.highsecure.vpn.proxy.master.main.SharedData
import com.highsecure.vpn.proxy.master.utils.Navigator
import timber.log.Timber
import kotlin.getValue

class PremiumFragment : ProductFragment<FragmentPremiumBinding>() {

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

    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        super.initView()

        premiumViewModel.loadData()

        skuAdapter.onItemClick = { pos, item ->
            Timber.d("item $pos ${item.productName} is selected")
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
                if (selectedSku.productName.isNotEmpty()) {
//                    premiumViewModel.launchSubscription(requireActivity(), selectedSku.productName)
                    mainViewModel.setSub(true)
                    requireActivity().finish()
                    //Navigator.startMainActivity(requireContext(), "")
                    Toast.makeText(requireContext(), com.highsecure.vpn.proxy.master.R.string.premium_purchase_success, Toast.LENGTH_SHORT).show()
                    (activity as? PremiumActivity)?.apply {
                        finish()
                    }
                } else {
                    Toast.makeText(requireContext(), com.highsecure.vpn.proxy.master.R.string.premium_select_sku, Toast.LENGTH_SHORT).show()
                }
            }
        }

        premiumViewModel.skus.observe(viewLifecycleOwner) { it ->
            if (it.isNotEmpty()) {
                skuAdapter.updateData(it)
            }
        }

        premiumViewModel.isLoading.observe(this) { it ->
            if (it == true) {
                binding.progress.setVisible(true)
            } else {
                binding.progress.setVisible(false)
            }
        }

        SharedData.isSub.observe(viewLifecycleOwner) { it ->
            if (it == true) {
                Timber.d("subs successfully")
            }
        }
    }
}