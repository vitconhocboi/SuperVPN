package com.simple.libads.admob.bannerads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.simple.libads.FrameAds
import com.simple.libads.base.bannerads.BannerAnchored
import com.simple.libads.base.bannerads.BannerLoader
import com.simple.libads.base.bannerads.BannerType
import com.simple.libads.gone
import java.util.UUID

class AdmobBannerLoader(bannerId: String): BannerLoader(bannerId = bannerId) {

    override fun loadAds(
        @BannerType bannerType: Int?,
        @BannerAnchored bannerAnchored: String?,
        context: Context,
        parent: FrameAds,
        onLoaded: ((isSuccess: Boolean) -> Unit)?,
        onSizeChange: ((size: Size) -> Unit)?,
        isGoneWhenFail: Boolean
    ) {
        if(!canRequest) {
            parent.gone()
            onLoaded?.invoke(false)
            onSizeChange?.invoke(Size(0,0))
            return
        }
        val bannerView = AdView(context)
        val adRequest = AdRequest.Builder()

        bannerView.adUnitId = bannerId
        val adSize : AdSize
        when(bannerType) {
            BannerType.TYPE_INLINE_ADAPTIVE -> {
                adSize = getBannerAdaptiveSize(context, parent)
                bannerView.setAdSize(adSize)
            }

            BannerType.TYPE_COLLAPSIBLE -> {
                adSize = getBannerAdaptiveSize(context, parent)
                bannerView.setAdSize(adSize)
                val extras = Bundle()
                extras.putString("collapsible", bannerAnchored ?: BannerAnchored.TYPE_ANCHORED_BOTTOM)
                extras.putString("collapsible_request_id", UUID.randomUUID().toString());
                adRequest
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            }

            else -> {
                adSize = getBannerAdaptiveSize(context, parent)
                bannerView.setAdSize(adSize)
            }

        }

        onSizeChange?.invoke(Size(adSize.width, adSize.height))

        bannerView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                onLoaded?.invoke(true)
            }


            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                if (isGoneWhenFail) {
                    parent.isVisible = false
                }
                onLoaded?.invoke(false)
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

        }
        parent.removeAllViews()
        parent.addView(bannerView)
        parent.layoutParams.height = dptopx(context, adSize.height.toFloat())
        bannerView.loadAd(adRequest.build())
    }

    fun dptopx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun getBannerAdaptiveSize(context: Context, parent: ViewGroup): AdSize {
        val activity  = context as Activity
        val display = activity.windowManager?.defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = parent.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    override fun destroy() {

    }
}