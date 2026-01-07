package com.tici.vpn.proxy.master.feature.feature_uninstall.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.core.ads.domain.AdLoadBannerNativeUiResource
import com.core.baseui.InsetsViewModel
import com.core.baseui.fragment.BaseChildOfHostFragment
import com.core.baseui.fragment.ScreenType
import com.core.baseui.fragment.collectFlowOn
import com.core.config.domain.data.CoreAdPlaceName
import com.core.config.domain.data.IAdPlaceName
import com.core.utilities.setOnSingleClick
import com.tici.vpn.proxy.master.databinding.CoreFragmentUninstallFeedbackBinding
import com.tici.vpn.proxy.master.feature.feature_uninstall.ui.navigate.UninstallNavigateEvent
import com.tici.vpn.proxy.master.main.MainActivity
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Constant
import com.tici.vpn.proxy.master.utils.Navigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UninstallFeedbackChildOfHostFragment:
    BaseChildOfHostFragment<CoreFragmentUninstallFeedbackBinding, UninstallNavigateEvent, UninstallShareViewModel>() {
    private val insetsViewModel: InsetsViewModel by activityViewModels()
    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): CoreFragmentUninstallFeedbackBinding {
        return CoreFragmentUninstallFeedbackBinding.inflate(inflater, container, false)
    }

    override val hostViewModel: UninstallShareViewModel by viewModels( ownerProducer = { requireParentFragment() })
    override val screenType: ScreenType
        get() = AppScreenType.ReasonUninstall

    private fun updateCheckedRdBtn() {
        val listParentView =
            listOf(
                binding.bgClRadio1,
                binding.bgClRadio2,
                binding.bgClRadio3,
                binding.bgClRadio4
            )
        val listRdBtn =
            listOf(
                binding.radio1,
                binding.radio2,
                binding.radio3,
                binding.radio4
            )

        listParentView.forEachIndexed { index, parentView ->
            parentView.setOnClickListener {
                // Uncheck tất cả
                listRdBtn.forEach { it.isChecked = false }
                try {
                    listRdBtn[index].isChecked = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateActiveBtnUninstall() {
        val listRdBtn =
            listOf(
                binding.radio1,
                binding.radio2,
                binding.radio3,
                binding.radio4
            )

        // Lọc ra các RadioButton đang được check
        val checkedButtons = listRdBtn.filter { it.isChecked }

        if (checkedButtons.isNotEmpty()) {
            binding.tvUninstall.isEnabled = true
            binding.tvUninstall.alpha = 1f
        } else {
            binding.tvUninstall.isEnabled = false
            binding.tvUninstall.alpha = 0.5f
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        binding.run {
            tvUninstall.setOnClickListener {
                // Tạo một Intent để mở màn hình chi tiết ứng dụng trong Cài đặt
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                // Chỉ định gói ứng dụng cần mở (chính là ứng dụng của bạn)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri

                // Bắt đầu Activity của hệ thống
                startActivity(intent)
            }

            btnBack.setOnClickListener {
                hostViewModel.navigateActionBack()
            }

            tvCancel.setOnClickListener {
                Navigator.navigateToMainActivity(requireActivity())
            }
        }
        collectFlowOn(insetsViewModel.systemInsets) { systemInsets ->
            systemInsets?.insets?.let {
                binding.root.setPadding(
                    it.left,
                    it.top,
                    it.right,
                    it.bottom
                )
            }
        }
        initEvent()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        binding.viewClickReturn.setOnClickListener {
            return@setOnClickListener
        }

        binding.viewClickReturn.setOnTouchListener { v, event ->
            return@setOnTouchListener false
        }

        updateCheckedRdBtn()

        binding.radio1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                updateActiveBtnUninstall()
        }

        binding.radio2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                updateActiveBtnUninstall()
        }

        binding.radio3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                updateActiveBtnUninstall()
        }

        binding.radio4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                updateActiveBtnUninstall()
        }


        binding.tvUninstall.setOnSingleClick {
            when {
                binding.radio1.isChecked -> {
                    (activity as? MainActivity)?.firebaseAnalytics?.logEvent(
                        Constant.FirebaseConstant.REASON_UNINSTALL + getReasonString(binding.radio1),
                        Bundle()
                    )
                }

                binding.radio2.isChecked -> {
                    (activity as? MainActivity)?.firebaseAnalytics?.logEvent(
                        Constant.FirebaseConstant.REASON_UNINSTALL + getReasonString(binding.radio2),
                        Bundle()
                    )
                }

                binding.radio3.isChecked -> {
                    (activity as? MainActivity)?.firebaseAnalytics?.logEvent(
                        Constant.FirebaseConstant.REASON_UNINSTALL + getReasonString(binding.radio3),
                        Bundle()
                    )
                }

                binding.radio4.isChecked -> {
                    (activity as? MainActivity)?.firebaseAnalytics?.logEvent(
                        Constant.FirebaseConstant.REASON_UNINSTALL + getReasonString(binding.radio4),
                        Bundle()
                    )
                }
            }
            // Tạo một Intent để mở màn hình chi tiết ứng dụng trong Cài đặt
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

            // Chỉ định gói ứng dụng cần mở (chính là ứng dụng của bạn)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri

            // Bắt đầu Activity của hệ thống
            startActivity(intent)
        }
    }

    private fun getReasonString(rdBtnChecked: RadioButton): String {
        return when (rdBtnChecked) {
            binding.radio1 -> {
                "difficult_to_use"
            }

            binding.radio2 -> {
                "too_many_ads"
            }

            binding.radio3 -> {
                "Too few VPN"
            }

            binding.radio4 -> {
                "VPN not working"
            }

            else -> {
                "difficult_to_use"
            }
        }
    }
    
    

    override fun providerBannerNativeAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_2
        )
    }

    override fun onBannerNativeResult(adResource: AdLoadBannerNativeUiResource) {
        binding.layoutBannerNative.processAdResource(adResource, CoreAdPlaceName.ANCHORED_UNINSTALL_BOTTOM_STEP_2)
    }
}