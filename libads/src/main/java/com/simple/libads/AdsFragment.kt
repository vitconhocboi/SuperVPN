package com.simple.libads

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class AdsFragment : Fragment() {


    fun showInterAds(
        placementId: String, showCallBack: ((isShow: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {},
    ) {
        (activity as? AdsActivity)?.showInterAds(
            placementId = placementId,
            showCallBack = showCallBack,
            impressedCallBack = impressedCallBack
        )
    }

    fun showInterWithoutVideoAds(
        placementId: String, showCallBack: ((isShow: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {},
    ) {
        (activity as? AdsActivity)?.showInterAdsWithoutVideo(
            placementId = placementId,
            showCallBack = showCallBack,
            impressedCallBack = impressedCallBack
        )
    }

    fun showRewardAds(
        placementId: String,
        showCallBack: ((isShow: Boolean, isClaim: Boolean) -> Unit),
        impressedCallBack: () -> Unit = {},
    ) {
        (activity as? AdsActivity)?.showRewardAds(
            placementId = placementId,
            showCallBack = showCallBack,
            impressedCallBack = impressedCallBack
        )
    }

    private var onLoadRewardSuccess: (() -> Unit)? = null
    private var onLoadRewardError: (() -> Unit)? = null
    fun isRewardReady() = (activity as? AdsActivity)?.isRewardReady() ?: false


    fun registerRewardLoad(onLoadSuccess: (() -> Unit)?, onLoadError: (() -> Unit)?) {
        onLoadRewardSuccess = onLoadSuccess
        onLoadRewardError = onLoadError
        (activity as? AdsActivity)?.registerRewardLoad(onLoadRewardSuccess, onLoadRewardError)
    }

    fun unregisterRewardLoad() {
        onLoadRewardSuccess = null
        onLoadRewardError = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterRewardLoad()
    }

    fun reloadRewardAds() {
        (activity as? AdsActivity)?.reloadRewardAds()
    }

}