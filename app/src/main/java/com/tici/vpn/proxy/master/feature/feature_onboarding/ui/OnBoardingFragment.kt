package com.tici.vpn.proxy.master.feature.feature_onboarding.ui

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
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment: BaseFragment<CoreFragmentOnboardingBinding>() {

    private val sharedViewModel: OnBoardingViewModel by activityViewModels()
    private val INTRO_PAGE_COUNT = 3
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

        binding.ivIntroduction.setImageResource(
            when (introductionPosition) {
                0 -> R.drawable.guide1
                1 -> R.drawable.guide2
                2 -> R.drawable.guide3
                else -> R.drawable.guide1
            }
        )
        binding.tvTitle.text = when (introductionPosition) {
            0 -> getString(R.string.core_onboarding_title_1)
            1 -> getString(R.string.core_onboarding_title_2)
            2 -> getString(R.string.core_onboarding_title_3)
            else -> getString(R.string.core_onboarding_title_1)
        }

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