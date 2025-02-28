package com.simple.libads.admob.nativeads.loader

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMaxCtaBottom
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumCtaBottom
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumCtaRight
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumCtaTop
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumCtaTopRight
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumMediaCtaRight
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumMediaCtaRightBottom
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumMediaCtaTop
import com.simple.libads.admob.nativeads.view.AdmobNativeAdMediumNoIconCtaBottom
import com.simple.libads.admob.nativeads.view.AdmobNativeAdSmallCtaBottom
import com.simple.libads.admob.nativeads.view.AdmobNativeAdSmallCtaRight
import com.simple.libads.admob.nativeads.view.AdmobNativeAdSmallCtaTop
import com.simple.libads.base.nativeads.AdsLoadingState
import com.simple.libads.base.nativeads.NativeLoader
import com.simple.libads.base.nativeads.NativeType
import com.simple.libads.databinding.ShimmerGntMediumCtaBottomBinding
import com.simple.libads.databinding.ShimmerGntMediumCtaRightBinding
import com.simple.libads.databinding.ShimmerGntMediumCtaRightTopBinding
import com.simple.libads.databinding.ShimmerGntMediumCtaTopBinding
import com.simple.libads.databinding.ShimmerGntMediumMediaCtaRightBinding
import com.simple.libads.databinding.ShimmerGntMediumNativeMaxCtaBottom12Binding
import com.simple.libads.databinding.ShimmerGntMediumNoIconCtaBottomBinding
import com.simple.libads.databinding.ShimmerGntMediumNoIconCtaRightBottomBinding
import com.simple.libads.databinding.ShimmerGntMediumNoIconCtaTopBinding
import com.simple.libads.databinding.ShimmerGntSmallCtaBottomBinding
import com.simple.libads.databinding.ShimmerGntSmallCtaRightBinding
import com.simple.libads.databinding.ShimmerGntSmallCtaTopBinding
import com.simple.libads.visible
import timber.log.Timber

open class AdmobNativeLoader(adsId: String, @NativeType nativeType: Int) : NativeLoader(adsId, nativeType) {

    private var mNativeAd: NativeAd? = null

    companion object {
        fun newInstance(adsId: String,@NativeType nativeType: Int): AdmobNativeLoader {
            return AdmobNativeLoader(adsId, nativeType)
        }
    }

    override fun bindPlaceHolder(view: ViewGroup, context: Context) {
        if(!canRequest) {
            view.removeAllViews()
            return
        }
        if(mNativeAd == null) {
            view.visible()
            view.changeSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = ViewGroup.LayoutParams.WRAP_CONTENT)
            view.removeAllViews()
            val shimmerView = when(nativeType) {
                NativeType.TYPE_SMALL_CTA_BOTTOM -> {
                    ShimmerGntSmallCtaBottomBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_SMALL_CTA_RIGHT -> {
                    ShimmerGntSmallCtaRightBinding.inflate(LayoutInflater.from(context)).root
                }


                NativeType.TYPE_SMALL_CTA_TOP -> {
                    ShimmerGntSmallCtaTopBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_CTA_BOTTOM -> {
                    ShimmerGntMediumCtaBottomBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_CTA_RIGHT -> {
                    ShimmerGntMediumCtaRightBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_CTA_TOP -> {
                    ShimmerGntMediumCtaTopBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_CTA_TOP_RIGHT -> {
                    ShimmerGntMediumCtaRightTopBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM, NativeType.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM_MAX_ROUND -> {
                    ShimmerGntMediumNoIconCtaBottomBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_RIGHT_BOTTOM -> {
                    ShimmerGntMediumNoIconCtaRightBottomBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_RIGHT -> {
                    ShimmerGntMediumMediaCtaRightBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_TOP -> {
                    ShimmerGntMediumNoIconCtaTopBinding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_MAX_CTA_BOTTOM -> {
                    ShimmerGntMediumNativeMaxCtaBottom12Binding.inflate(LayoutInflater.from(context)).root
                }

                NativeType.TYPE_CUSTOM -> {
                    createCustomHolder(context) ?: throw Exception("Please override fun createCustomNativeView when use TYPE_CUSTOM ")
                }

                else -> throw Exception("unkown ads")
            }
            shimmerView.startShimmer()
            view.addView(shimmerView)
        }
    }

    override fun bind(view: ViewGroup, context: Context) {
        if(!canRequest) {
            view.removeAllViews()
            return
        }
        if (mNativeAd != null ) {
            view.visible()
            view.changeSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = ViewGroup.LayoutParams.WRAP_CONTENT)
            view.removeAllViews()
            val adsView = when(nativeType) {
                NativeType.TYPE_SMALL_CTA_BOTTOM -> {
                    AdmobNativeAdSmallCtaBottom(context, null)
                }

                NativeType.TYPE_SMALL_CTA_RIGHT -> {
                    AdmobNativeAdSmallCtaRight(context, null)
                }


                NativeType.TYPE_SMALL_CTA_TOP -> {
                    AdmobNativeAdSmallCtaTop(context, null)
                }

                NativeType.TYPE_MEDIUM_CTA_BOTTOM -> {
                    AdmobNativeAdMediumCtaBottom(context, null)
                }

                NativeType.TYPE_MEDIUM_CTA_RIGHT -> {
                    AdmobNativeAdMediumCtaRight(context, null)
                }

                NativeType.TYPE_MEDIUM_CTA_TOP -> {
                    AdmobNativeAdMediumCtaTop(context, null)
                }

                NativeType.TYPE_MEDIUM_CTA_TOP_RIGHT -> {
                    AdmobNativeAdMediumCtaTopRight(context, null)
                }

                NativeType.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM -> {
                    AdmobNativeAdMediumNoIconCtaBottom(context, null)
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_RIGHT_BOTTOM -> {
                    AdmobNativeAdMediumMediaCtaRightBottom(context, null)
                }

                NativeType.TYPE_MEDIUM_NO_ICON_CTA_BOTTOM_MAX_ROUND -> {
                    AdmobNativeAdMediumNoIconCtaBottom(context, null).apply {
                        setRoundedMaxCorners()
                    }
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_RIGHT -> {
                    AdmobNativeAdMediumMediaCtaRight(context, null)
                }

                NativeType.TYPE_MEDIUM_MEDIA_CTA_TOP -> {
                    AdmobNativeAdMediumMediaCtaTop(context, null)
                }

                NativeType.TYPE_MAX_CTA_BOTTOM -> {
                    AdmobNativeAdMaxCtaBottom(context, null)
                }

                NativeType.TYPE_CUSTOM-> {
                    createCustomNativeView(context) ?: throw Exception("Please override fun createCustomNativeView when use TYPE_CUSTOM ")
                }

                else -> throw Exception("unkown ads")

            }
            adsView.setNativeAd(nativeAd = mNativeAd!!)
            view.addView(adsView)
        }
    }

    override fun isReady(): Boolean {
        return mNativeAd != null
    }

    override fun isLoadFailed(): Boolean {
        return liveUpdate.value == AdsLoadingState.STATE_ERROR
    }

    override fun loadAds(context: Context) {
        if(!canRequest) {
            liveUpdate.postValue(AdsLoadingState.STATE_ERROR)
            return
        }
        if (!isLoading && (mNativeAd == null || System.currentTimeMillis() - timeLoaded > TIME_OUT_NATIVE)) {
            isLoading = true
            liveUpdate.postValue(AdsLoadingState.STATE_LOADING)
            val adId = adsId
            val videoOptions =
                VideoOptions.Builder().setStartMuted(true).build()

            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            val adLoader = AdLoader.Builder(context, adId)
                .forNativeAd { nativeAd ->
                    if (mNativeAd != null) {
                        mNativeAd?.destroy()
                    }
                    mNativeAd = nativeAd
                    isLoading = false
                    liveUpdate.postValue(AdsLoadingState.STATE_LOADED)
                    timeLoaded = System.currentTimeMillis()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        Timber.e("onAdFailedToLoad: load native ad failed : code = $p0")
                        isLoading = false
                        if (mNativeAd == null) {
                            liveUpdate.postValue(AdsLoadingState.STATE_ERROR)
                        }
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Timber.e("onAdLoaded: load native ad onAdLoaded")
                        isLoading = false
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                    }
                })
                .withNativeAdOptions(adOptions).build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    override fun reloadAds(context: Context) {
        if(!canRequest) {
            return
        }
        if (!isLoading && (mNativeAd == null || System.currentTimeMillis() - timeLoaded > TIME_OUT_NATIVE)) {
            liveUpdate.postValue(AdsLoadingState.STATE_LOADING)
            isLoading = true
            val adId = adsId
            val videoOptions =
                VideoOptions.Builder().setStartMuted(true).build()

            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            val adLoader = AdLoader.Builder(context, adId)
                .forNativeAd { nativeAd ->
                    if (mNativeAd != null) {
                        mNativeAd?.destroy()
                    }
                    mNativeAd = nativeAd
                    isLoading = false
                    liveUpdate.postValue(AdsLoadingState.STATE_LOADED)
                    timeLoaded = System.currentTimeMillis()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        Timber.e("onAdFailedToLoad: load native ad failed : code = $p0")
                        isLoading = false
                        if (mNativeAd == null) {
                            liveUpdate.postValue(AdsLoadingState.STATE_ERROR)
                        }
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Timber.e("onAdLoaded: load native ad onAdLoaded")
                        isLoading = false
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                    }
                })
                .withNativeAdOptions(adOptions).build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }
}