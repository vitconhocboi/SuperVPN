package com.tici.vpn.proxy.master.proxy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.common.baseui.view.RadioGroupManager
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.config.domain.data.IAdPlaceName
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.TabProxyFragmentBinding
import com.tici.vpn.proxy.master.proxy.fragments.ProxyPageAdapter
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TabProxyFragment : BaseFragment<TabProxyFragmentBinding>() {

    private val mRadioGroupManager = RadioGroupManager()

    private lateinit var mPagerAdapter: ProxyPageAdapter


    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): TabProxyFragmentBinding {
        return TabProxyFragmentBinding.inflate(inflater, container, false)
    }

    override fun providerInterAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.FULLSCREEN_BACK_TAB_PROXY
        )
    }

    override val screenType: ScreenType
        get() = AppScreenType.TabProxyFragment

    @SuppressLint("HardwareIds")
    override fun initViews(savedInstanceState: Bundle?) {

        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        mPagerAdapter =
            ProxyPageAdapter(fragmentManager = childFragmentManager, lifecycle = lifecycle)

        mPagerAdapter.updateData(
            arrayListOf(
                PremiumProxyFragment(),
                GameProxyFragment()
            )
        )

        with(binding) {

            ivBack.scaleX = if (isRTL) -1f else 1f

            vpProxies.adapter = mPagerAdapter
            vpProxies.offscreenPageLimit = 1

            mRadioGroupManager.addRadio(RadioGroupManager.Radio().addViewParent(tabFree).setTag(0))
            mRadioGroupManager.addRadio(
                RadioGroupManager.Radio().addViewParent(tabPremium).setTag(1)
            )

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
                showInterAd(AppAdPlaceName.FULLSCREEN_BACK_TAB_PROXY) {
                    Navigator.startMainActivity(requireActivity())
                    activity?.finish()
                }
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
                    tabAll.isSelected = true
                    tabRecommend.isSelected = false
//                    tabFree.setTypeface(typeBold)
//                    tabFree.setTextColor(requireContext().getColor(R.color.textColor))
//                    tabPremium.setTypeface(typeNormal)
//                    tabPremium.setTextColor(requireContext().getColor(R.color.text_gray_2))
//                    lnFree.visibility = View.VISIBLE
//                    lnPremium.visibility = View.INVISIBLE
                }

                1 -> {
                    tabAll.isSelected = false
                    tabRecommend.isSelected = true
//                    tabPremium.setTypeface(typeBold)
//                    tabPremium.setTextColor(requireContext().getColor(R.color.textColor))
//                    tabFree.setTypeface(typeNormal)
//                    tabFree.setTextColor(requireContext().getColor(R.color.text_gray_2))
//                    lnPremium.visibility = View.VISIBLE
//                    lnFree.visibility = View.INVISIBLE
                }
            }
        }
    }
}