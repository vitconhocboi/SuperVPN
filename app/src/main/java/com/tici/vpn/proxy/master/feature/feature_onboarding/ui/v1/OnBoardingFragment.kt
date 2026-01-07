package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.tici.vpn.proxy.master.R
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.argument
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.databinding.CoreFragmentOnboardingBinding
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory.INTRO_PAGE_COUNT
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment: BaseFragment<CoreFragmentOnboardingBinding>() {

    private val sharedViewModel: OnBoardingViewModel by activityViewModels()
    companion object {
        fun newInstance(position: Int) = OnBoardingFragment().apply {
            this.introductionPosition = position
        }
    }

    private var introductionPosition by argument<Int>()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentOnboardingBinding {
        return CoreFragmentOnboardingBinding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.OnBoarding

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        binding.ivIntroduction.setImageResource(OnBoardingConfigFactory.getImageResIntro(introductionPosition))
        binding.tvTitle.text = getString(OnBoardingConfigFactory.getStringIntro(introductionPosition))

        binding.dotsIndicator.setCountPage(INTRO_PAGE_COUNT)
        binding.dotsIndicator.setPage(introductionPosition)

        binding.tvNext.text =  if(introductionPosition == INTRO_PAGE_COUNT - 1) {
            getString(R.string.core_onboarding_action_get_start)
        } else {
            getString(R.string.core_onboarding_action_next)
        }

        binding.tvNext.setOnSingleClick {
            if(introductionPosition == INTRO_PAGE_COUNT - 1) {
                sharedViewModel.navigateTo(OnBoardingEvent.FinishStep)
            } else {
                sharedViewModel.navigateTo(OnBoardingEvent.NextEvent)
            }
        }

    }

}