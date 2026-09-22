package com.core.baseui

import android.app.Application
import android.os.Build
import android.text.TextUtils
import android.webkit.WebView
import com.core.config.BuildConfig
import com.core.preference.AppPreferences
import com.core.preference.SharedPrefs
import com.core.utilities.util.Timber
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.analytics.analytics
import java.util.EnumMap
import java.util.Locale
import javax.inject.Inject

abstract class BaseCoreApplication : Application() {

    @Inject
    lateinit var appPreferences: AppPreferences
    open val requiredUpdateConsent = true

    override fun onCreate() {
        super.onCreate()

        SharedPrefs.init(context = this, name = "Core")

        if (appPreferences.systemLanguageCode.isBlank()) {
            appPreferences.systemLanguageCode = Locale.getDefault().language
        }

        fixWebView()

        setupConsentMode()

        initLogging()

        initOtherConfig()
    }

    /**
     * Sửa lỗi Android Pie (9.0) WebView in multi-process
     *
     * https://stackoverflow.com/questions/51843546/android-pie-9-0-webview-in-multi-process
     */
    open fun fixWebView() {
        if (TextUtils.isEmpty(packageName)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val process = getProcessName()
            if (packageName != process) WebView.setDataDirectorySuffix(process)
        }
    }

    /**
     * Khởi tạo các cấu hình khác
     */
    open fun initOtherConfig() {}

    open fun initLogging() {
        if (Timber.treeCount != 0) return
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    /**
     * Cấu hình analytics storage cho Firebase
     */
    private fun setupConsentMode() {
        if (!requiredUpdateConsent) return
        EnumMap<ConsentType, FirebaseAnalytics.ConsentStatus>(ConsentType::class.java).apply {
            put(ConsentType.ANALYTICS_STORAGE, FirebaseAnalytics.ConsentStatus.GRANTED)
        }.let(Firebase.analytics::setConsent)
    }

    companion object {
        var isFirstSaveLanguage = false
        var isUserSelectLanguageNotDefault = false
    }
}
