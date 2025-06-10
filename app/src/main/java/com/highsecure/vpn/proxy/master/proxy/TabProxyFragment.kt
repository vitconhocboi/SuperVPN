package com.highsecure.vpn.proxy.master.proxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.highsecure.vpn.proxy.master.base.ProductFragment
import com.highsecure.vpn.proxy.master.databinding.TabProxyFragmentBinding
import com.highsecure.vpn.proxy.master.proxy.FreeProxyFragment
import com.highsecure.vpn.proxy.master.utils.Navigator
import com.common.baseui.view.RadioGroupManager
import com.highsecure.vpn.proxy.master.R
import com.highsecure.vpn.proxy.master.proxy.PremiumProxyFragment
import com.highsecure.vpn.proxy.master.proxy.fragments.ProxyPageAdapter

class TabProxyFragment : ProductFragment<TabProxyFragmentBinding>() {

    private val mRadioGroupManager = RadioGroupManager()

    private lateinit var mPagerAdapter: ProxyPageAdapter


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): TabProxyFragmentBinding {
        return TabProxyFragmentBinding.inflate(inflater, container, false)
    }

    @SuppressLint("HardwareIds")
    override fun initView() {
        super.initView()

        mPagerAdapter =
            ProxyPageAdapter(fragmentManager = childFragmentManager, lifecycle = lifecycle)

        mPagerAdapter.updateData(
            arrayListOf(
                FreeProxyFragment(),
                PremiumProxyFragment()
            )
        )

        with(binding) {
            vpProxies.adapter = mPagerAdapter
            vpProxies.offscreenPageLimit = 1

            mRadioGroupManager.addRadio(RadioGroupManager.Radio().addViewParent(tabFree).setTag(0))
            mRadioGroupManager.addRadio(RadioGroupManager.Radio().addViewParent(tabPremium).setTag(1))

            mRadioGroupManager.setRadioChange { view, tag ->
                onSelectedTab(tag as Int)
                vpProxies.setCurrentItem(tag as Int, true)
            }

            vpProxies.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onSelectedTab(position)
                    mRadioGroupManager.selectedRadioByTag(position)
                }
            })

            ivBack.setOnClickListener {
                Navigator.startMainActivity(requireActivity())
            }
        }
    }

    fun onSelectedTab(tag: Int) {
        with(binding) {
            val typeNormal =
                ResourcesCompat.getFont(requireContext(), R.font.inter_normal)
            val typeBold = ResourcesCompat.getFont(
                requireContext(), R.font.inter_bold
            )


            when (tag) {
                0 -> {
                    tabFree.setTypeface(typeBold)
                    tabFree.setTextColor(requireContext().getColor(R.color.textColor))
                    lnFree.visibility = View.VISIBLE
                    tabPremium.setTypeface(typeNormal)
                    tabPremium.setTextColor(requireContext().getColor(R.color.text_gray_2))
                    lnPremium.visibility = View.INVISIBLE
                }

                1 -> {
                    tabPremium.setTypeface(typeBold)
                    tabPremium.setTextColor(requireContext().getColor(R.color.textColor))
                    lnPremium.visibility = View.VISIBLE
                    tabFree.setTypeface(typeNormal)
                    tabFree.setTextColor(requireContext().getColor(R.color.text_gray_2))
                    lnFree.visibility = View.INVISIBLE
                }
            }
        }
    }
}