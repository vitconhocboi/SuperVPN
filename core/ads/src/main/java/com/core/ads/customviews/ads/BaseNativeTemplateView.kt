package com.core.ads.customviews.ads

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.gms.ads.nativead.NativeAd

abstract class BaseNativeTemplateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    abstract fun destroyNativeAd()

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        try {
//            removeAllViews()
//            destroyNativeAd()
//        } catch (e: Exception) {
//            Log.e("BaseNativeTemplateView", "onDetachedFromWindow : $e")
//        }

    }

    abstract fun setNativeAd(nativeAd: NativeAd)

    /**
     * To prevent memory leaks, make sure to destroy your ad when you don't need it anymore. This
     * method does not destroy the template view.
     * https://developers.google.com/admob/android/native-unified#destroy_ad
     */
    abstract fun applyStyles(styles: NativeTemplateStyle)

    fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }
}