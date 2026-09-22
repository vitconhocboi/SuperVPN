package com.tici.vpn.proxy.master.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.common.baseui.BaseAppConfig

/**
 * Device ID gửi lên server proxy.
 * Dùng ANDROID_ID (không phụ thuộc Advertising ID) và cache vào [BaseAppConfig.deviceId],
 * nên user cũ vẫn giữ ID đã lưu trước đó.
 */
object DeviceIdProvider {

    @SuppressLint("HardwareIds")
    fun get(context: Context): String {
        BaseAppConfig.deviceId.takeIf { it.isNotEmpty() }?.let { return it }
        val androidId = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty()
        if (androidId.isNotEmpty()) BaseAppConfig.deviceId = androidId
        return androidId
    }
}