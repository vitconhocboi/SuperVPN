package com.simple.libads.admob.nativeads.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView


abstract class BaseNativeAdView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    open fun setNativeAd(nativeAd: NativeAd) {

        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon

        val secondaryText: String
        val nativeAdView = getAdView()

        nativeAdView.callToActionView = getCallActionButtonView()
        nativeAdView.headlineView = getPrimaryView()
        nativeAdView.mediaView = getMediaView()
        getSecondaryView()?.visibility = VISIBLE
        getCallActionButtonView().visibility = VISIBLE
        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = getSecondaryView()
            secondaryText = store!!
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = getSecondaryView()
            secondaryText = advertiser!!
        } else {
            secondaryText = ""
        }
        getPrimaryView().text = headline
        getCallActionButtonView().text = cta

        //  Set the secondary view to be the star rating if available.
        if (starRating != null && starRating > 0) {
            getSecondaryView()?.visibility = GONE
            getRatingView()?.visibility = VISIBLE
            getRatingView()?.rating = starRating.toFloat()
            nativeAdView.starRatingView = getRatingView()
        } else {
            getSecondaryView()?.text = secondaryText
            getSecondaryView()?.visibility = VISIBLE
            getRatingView()?.visibility = GONE
        }

        if (icon != null) {
            getIconView()?.visibility = VISIBLE
            getIconView()?.setImageDrawable(icon.drawable)
        } else {
            getIconView()?.visibility = GONE
        }

        if (getTertiaryView() != null) {
            getTertiaryView()?.text = body
            nativeAdView.bodyView = getTertiaryView()
        }

        getAdView().setNativeAd(nativeAd)
    }

    abstract fun getPrimaryView(): AppCompatTextView
    abstract fun getSecondaryView(): AppCompatTextView?
    abstract fun getRatingView(): AppCompatRatingBar?
    abstract fun getIconView(): ImageView?
    abstract fun getTertiaryView(): AppCompatTextView?
    abstract fun getCallActionButtonView(): AppCompatTextView
    open fun getViewContainerRate_Price(): View ?= null
    abstract fun getAdView(): NativeAdView
    open fun getMediaView(): MediaView? = null

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }
}