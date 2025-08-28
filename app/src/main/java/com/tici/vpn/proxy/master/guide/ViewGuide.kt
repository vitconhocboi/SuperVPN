package com.tici.vpn.proxy.master.guide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.baseui.BaseAppConfig
import com.common.baseui.BindingSheetDialog
import com.common.baseui.extension.setOnClickNoDoubleClick
import com.simple.libads.FrameAds
import com.simple.libads.base.bannerads.BannerLoader
import com.simple.libads.manager.BannerManager
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentViewGuideBinding
import com.tici.vpn.proxy.master.remoteconfig.AdPlacementId
import com.tici.vpn.proxy.master.remoteconfig.FirebaseConfigManager

class ViewGuide : BindingSheetDialog<FragmentViewGuideBinding>() {
    var configAd: FirebaseConfigManager = FirebaseConfigManager.get()
    private var mGuideModels = ArrayList<GuideModel>()
    private var mGuideAdapter = GuideAdapter()
    private var mBannerLoader: BannerLoader? = null

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentViewGuideBinding {

        return FragmentViewGuideBinding.inflate(inflater, container, false)
    }

    override fun getTheme() = com.common.baseui.R.style.CustomBottomSheetDialogTheme

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun initView() {
        mGuideModels.add(GuideModel(R.drawable.guide1, R.string.guide_1))
        mGuideModels.add(GuideModel(R.drawable.guide2, R.string.guide_2))
        mGuideModels.add(GuideModel(R.drawable.guide3, R.string.guide_3))
        mGuideAdapter.updateData(mGuideModels)
        binding.apply {
            vpGuide.adapter = mGuideAdapter
            ivBack.setOnClickNoDoubleClick {
                if (vpGuide.currentItem == 2) {
                    dismiss()
                } else {
                    vpGuide.setCurrentItem(vpGuide.currentItem + 1, true)
                }
            }
            indicator.attachTo(vpGuide)
            if (getResources().configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                ivBack.rotationY = 0f
            } else {
                ivBack.rotationY = 180f
            }
        }
        BaseAppConfig.isCanShowGuide = false
    }

    private fun initLoadAds(frameBanner : FrameAds) {
        configAd.adBanner.find { it.placementId == AdPlacementId.BANNER_HOME }?.let {
            mBannerLoader = BannerManager.createLoader(adNetwork = it.adNetwork, bannerId = it.adId)
            mBannerLoader?.canRequest = true
            try {
                mBannerLoader?.loadAds(
                    context = requireContext(), bannerType = it.adType, parent = frameBanner
                )
            } catch (e: Exception) {}
        }
    }
}