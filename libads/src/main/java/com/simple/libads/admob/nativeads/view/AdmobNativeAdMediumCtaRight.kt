package com.simple.libads.admob.nativeads.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView

import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import com.simple.libads.databinding.GntMediumCtaRight4Binding


class AdmobNativeAdMediumCtaRight(context: Context, attrs: AttributeSet?) : BaseNativeAdView(context, attrs) {
    private val binding = GntMediumCtaRight4Binding.inflate(LayoutInflater.from(context), this, true)

    override fun getPrimaryView(): AppCompatTextView {
        return binding.primary
    }

    override fun getSecondaryView(): AppCompatTextView? {
        return null
    }

    override fun getRatingView(): AppCompatRatingBar? {
        return null
    }

    override fun getTertiaryView(): AppCompatTextView {
        return binding.body
    }

    override fun getIconView(): ImageView = binding.icon

    override fun getCallActionButtonView(): AppCompatTextView = binding.cta

    override fun getAdView(): NativeAdView = binding.nativeAdView

    override fun getMediaView(): MediaView = binding.mediaView
}