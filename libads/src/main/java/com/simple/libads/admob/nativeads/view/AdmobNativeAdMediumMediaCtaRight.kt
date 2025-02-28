package com.simple.libads.admob.nativeads.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.gms.ads.nativead.MediaView


import com.google.android.gms.ads.nativead.NativeAdView
import com.simple.libads.databinding.GntMediumMediaCtaRight9Binding


class AdmobNativeAdMediumMediaCtaRight(context: Context, attrs: AttributeSet?) : BaseNativeAdView(context, attrs) {
    private val binding = GntMediumMediaCtaRight9Binding.inflate(LayoutInflater.from(context), this, true)

    override fun getPrimaryView(): AppCompatTextView {
        return binding.primary
    }

    override fun getSecondaryView(): AppCompatTextView? {
        return null
    }

    override fun getRatingView(): AppCompatRatingBar? {
        return binding.ratingBar
    }

    override fun getMediaView(): MediaView? {
        return binding.mediaView
    }

    override fun getTertiaryView(): AppCompatTextView = binding.body

    override fun getIconView(): ImageView? = null

    override fun getCallActionButtonView(): AppCompatTextView = binding.cta

    override fun getAdView(): NativeAdView = binding.nativeAdView

    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
    }
}