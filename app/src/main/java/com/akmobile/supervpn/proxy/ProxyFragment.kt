package com.akmobile.supervpn.proxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentProxyBinding
import com.akmobile.supervpn.home.ProxyGroupUI
import com.akmobile.supervpn.home.ProxyUI
import com.akmobile.supervpn.utils.Constant
import com.akmobile.supervpn.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProxyFragment : ProductFragment<FragmentProxyBinding>() {

    @Inject
    lateinit var mPagerAdapter: ProxyGroupAdapter
    var list = mutableListOf<ProxyGroupUI>(
        ProxyGroupUI("1", "United States", "us",
            list = mutableListOf(
                    ProxyUI("1",Constant.PROXY_HTTP,"United States","5.181.164.131","56789","US443764","oQFW9r2p"),
                    ProxyUI("2",Constant.PROXY_HTTP,"United States","193.39.184.34","56789","US391942","gXnU4s4k"),
                    ProxyUI("3",Constant.PROXY_HTTP,"United States","185.202.174.179","56789","US178623","pMKZ8q6b"),
                    ProxyUI("4",Constant.PROXY_SOCKS5,"United States","193.39.184.60","55555","US126173","dSBH7c4I"),
                    ProxyUI("5",Constant.PROXY_SOCKS5,"United States","193.39.184.80","55555","US264150","eTwU4h4p"),
            ),
            collapsed = true
        )
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
                Navigator.startMainActivity(requireActivity(), null)
            }
            ivReload.setOnClickListener {

            }
            rcvGroupProxy.adapter = mPagerAdapter
            mPagerAdapter.updateData(list)

            mPagerAdapter.onProxyClick = { position, item ->
                Navigator.startProxyActivity(requireContext(), item.id)
            }
        }
    }
}