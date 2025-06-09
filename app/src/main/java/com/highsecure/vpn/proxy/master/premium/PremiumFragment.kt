package com.highsecure.vpn.proxy.master.premium

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.FragmentPremiumBinding
import timber.log.Timber
import kotlin.getValue

class PremiumFragment : ProductFragment<FragmentPremiumBinding>() {

    private val premiumViewModel: PremiumViewModel by viewModels()

    private val skuAdapter = SkuAdapter()

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
        }

        binding.apply {

            rcvSkus.adapter = skuAdapter

            btnClose.setOnClickListener {
                (activity as? PremiumActivity)?.apply {
                    finish()
                }
            }
        }

        premiumViewModel.skus.observe(viewLifecycleOwner) { it ->
            if (it.isNotEmpty()) {
                skuAdapter.updateData(it)
            }
        }
    }
}