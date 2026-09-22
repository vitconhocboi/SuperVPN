package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.model.OnBoardingItem
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2.OnBoardingFragment2

class OnBoardingPagerAdapter2(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    var items: List<OnBoardingItem>
) : FragmentStateAdapter(fm, lifecycle) {


    override fun getItemCount(): Int {
        return items.size
    }


    override fun createFragment(position: Int): Fragment {
        return OnBoardingFragment2.Companion.newInstance(items[position].position)
    }
}