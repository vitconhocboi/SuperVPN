package com.hoangsinh.supervpn.home

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hoangsinh.supervpn.ProductFragment
import com.hoangsinh.supervpn.databinding.FragmentHomeBinding

class HomeFragment : ProductFragment<FragmentHomeBinding>() {
    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
    }
}