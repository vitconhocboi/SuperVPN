package com.akmobile.supervpn.settings.appproxy

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akmobile.supervpn.ProductFragment
import com.akmobile.supervpn.databinding.FragmentAppProxyBinding

class AppProxyFragment(val list: ArrayList<AppInfo>)  : ProductFragment<FragmentAppProxyBinding>() {

    private lateinit var appProxyAdapter: AppProxyAdapter

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAppProxyBinding {
        return FragmentAppProxyBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        appProxyAdapter = AppProxyAdapter()
        appProxyAdapter.submitList(list)
        binding.apply {
            recyclerViewApps.adapter = appProxyAdapter
        }
    }
}