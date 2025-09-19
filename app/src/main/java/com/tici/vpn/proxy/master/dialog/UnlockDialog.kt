package com.tici.vpn.proxy.master.dialog

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.lifecycleScope
import com.common.baseui.extension.context
import com.core.baseui.BaseAdsBottomSheetDialogFragment
import com.core.config.domain.data.IAdPlaceName
import com.core.utilities.setOnSingleClick
import com.core.utilities.toast
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.DialogUnlockBinding
import com.tici.vpn.proxy.master.proxy.ProxyGroupUI
import com.tici.vpn.proxy.master.required.ads.AppAdPlaceName
import com.tici.vpn.proxy.master.utils.Constant.KEY_RESULT_CONNECT_VPN
import com.tici.vpn.proxy.master.utils.Navigator
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UnlockDialog(
    val data: ProxyGroupUI
) : BaseAdsBottomSheetDialogFragment<DialogUnlockBinding>() {

    override fun providerRewardAdPlaceName(): List<IAdPlaceName> {
        return listOf(
            AppAdPlaceName.REWARDED_CONNECT_VPN
        )
    }

    override fun bindingProvider(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogUnlockBinding {
        return DialogUnlockBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unlock_bg)
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.cornerRadius = 20f

        binding.apply {
            ivFlag.setImageResource(Utils.getFlag(data.country))
            countryTitle.text =
                getStringSafely(context = context, name = data.country) ?: "undefined"
            lnGetPremium.background = roundedDrawable

            lnGetPremium.setOnClickListener {
                Navigator.startPremiumActivity(requireContext())
                closeDialog()
            }

            btnClose.setOnClickListener {
                closeDialog()
            }

            bgLlWatchAds.setOnSingleClick {
                showRewardAd(adPlaceName = AppAdPlaceName.REWARDED_CONNECT_VPN) { isShown, isEarnedReward ->
                    if (isEarnedReward) {
                        callBackConnectVpn()
                        closeDialog()
                    } else if (isShown) {
                        activity?.toast(R.string.connect_error)
                    } else {
                        //
                    }
                }

//                fun unlockWithRewarded(callbackSuccess: () -> Unit, callbackClose: (() -> Unit)? = null) {
//                    showRewardAd(adPlaceName = AppAdPlaceName.REWARDED_CONNECT_VPN, onHandleCompleted = { isShown, isEarnedReward ->
//                        if (isEarnedReward) {
//                            callbackSuccess.invoke()
//                        } else if (isShown) {
//                            callbackClose?.invoke()
//                            cancelRewarded()
//                        } else {
//                            unlockDownloadShareViewModel.unlockFailed(isNetworkError = false)
//                        }
//                    })
//                }
            }
        }
    }

    fun closeDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isAdded) {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun callBackConnectVpn() {
        val bundleBack = Bundle()
        parentFragmentManager.setFragmentResult(
            KEY_RESULT_CONNECT_VPN,
            bundleBack
        )
    }

    private fun getStringSafely(context: Context, name: String): String? {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

}