package com.akmobile.supervpn.proxy

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentProxyBinding
import com.akmobile.supervpn.home.ProxyGroupUI
import com.akmobile.supervpn.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyFragment : ProductFragment<FragmentProxyBinding>() {
    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter
    var list = mutableListOf<ProxyGroupUI>(
        ProxyGroupUI("1", "United States", "us"),
        ProxyGroupUI("2", "Viet Nam", "vietnam"),
        ProxyGroupUI("3", "Korea", "korea"),
        ProxyGroupUI("4", "Japan", "japan"),
        ProxyGroupUI("5", "India", "india"),
        ProxyGroupUI("6", "Arab", "arab"),
    )

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProxyBinding {
        return FragmentProxyBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        with(binding) {
            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity())
            }
            ivReload.setOnClickListener {

            }
            rcvGroupProxy.adapter = mPagerAdapter
            mPagerAdapter.updateData(list)
        }
    }
}