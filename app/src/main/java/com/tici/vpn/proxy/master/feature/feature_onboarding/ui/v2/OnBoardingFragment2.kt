package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v2

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.argument
import com.core.utilities.gone
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.CoreFragmentOnboardingV2Binding
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory.INTRO_PAGE_COUNT
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingEvent
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.v1.OnBoardingViewModel
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment2 : BaseFragment<CoreFragmentOnboardingV2Binding>() {

    private val sharedViewModel: OnBoardingViewModel by activityViewModels()

    companion object {
        fun newInstance(position: Int) = OnBoardingFragment2().apply {
            this.introductionPosition = position
        }
    }

    private var introductionPosition by argument<Int>()

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): CoreFragmentOnboardingV2Binding {
        return CoreFragmentOnboardingV2Binding.inflate(inflater, container, false)
    }

    override val screenType: ScreenType
        get() = AppScreenType.OnBoarding

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        binding.ivIntroduction.setImageResource(
            OnBoardingConfigFactory.getImageResIntro(
                introductionPosition
            )
        )
        binding.tvTitle.text =
            getString(OnBoardingConfigFactory.getStringIntro(introductionPosition))
        OnBoardingConfigFactory.getSubtitleIntro(introductionPosition)?.let {
            binding.tvTitle2.text = getString(it)
        } ?: run {
            binding.tvTitle2.gone()
        }


        binding.tvNext.text = if (introductionPosition == INTRO_PAGE_COUNT - 1) {
            /**Set gradient cho button start*/
            binding.tvNext.setGradientStrokeBackground(
                "#0EC74B".toColorInt(),
                "#0EC74B".toColorInt(),
                strokeWidthDp = 1.5f,
                cornerRadiusDp = 100f
            )
            binding.tvNext.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.core_appColor
                )
            )
            binding.layoutContent.setBackgroundColor(Color.TRANSPARENT)
            getString(R.string.core_onboarding_action_get_start)
        } else {
            binding.tvNext.setFillGradientEnabled(false)
            binding.tvNext.setTextColor(Color.WHITE)
            binding.tvNext.setBackgroundResource(R.drawable.bg_start_trial)
            getString(R.string.core_onboarding_action_next)
        }

        binding.tvNext.setOnSingleClick {
            if (introductionPosition == INTRO_PAGE_COUNT - 1) {
                sharedViewModel.navigateTo(OnBoardingEvent.FinishStep)
            } else {
                sharedViewModel.navigateTo(OnBoardingEvent.NextEvent)
            }
        }
    }

}