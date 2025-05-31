package com.highsecure.vpn.proxy.master.proxy.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.common.baseui.adapter.PagerAdapter

class ProxyPageAdapter (
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : PagerAdapter<Fragment>(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return data[position]
    }
}