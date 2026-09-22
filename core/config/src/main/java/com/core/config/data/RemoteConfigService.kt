package com.core.config.data

import com.core.config.BuildConfig
import com.squareup.moshi.Moshi
import com.core.config.R
import com.core.config.data.helper.ConfigParam
import com.core.config.data.helper.read
import com.core.config.data.model.AppConfigModel
import com.core.config.data.model.IapConfigModel
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigService @Inject constructor(
    private val moshi: Moshi
) {

    companion object {
        const val CONFIG_CACHE_EXPIRATION_SECONDS = 1L
    }
//
//    private val config = FirebaseRemoteConfig.getInstance().apply {
//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(CONFIG_CACHE_EXPIRATION_SECONDS)
//            .build()
//        setConfigSettingsAsync(configSettings)
//        fetch(CONFIG_CACHE_EXPIRATION_SECONDS)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    fetchAndActivate()
//                }
//            }
//    }

    private val remoteConfig by lazy {
        val settings = remoteConfigSettings {
            fetchTimeoutInSeconds = 30
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG || BuildConfig.FLAVOR == "dev") {
                0
            } else {
                CONFIG_CACHE_EXPIRATION_SECONDS
            }
        }
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(settings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }

    // Extension reified để gọi gọn
    inline fun <reified T> fetchOtherConfig(key: String): T? {
        return fetchOtherConfig(key, T::class.java)
    }

    fun <T> fetchOtherConfig(key: String, clazz: Class<T>): T? {
        return try {
            when (clazz) {
                String::class.java -> clazz.cast(remoteConfig.getString(key))
                Boolean::class.java -> clazz.cast(remoteConfig.getBoolean(key))
                Long::class.java -> clazz.cast(remoteConfig.getLong(key))
                Double::class.java -> clazz.cast(remoteConfig.getDouble(key))
                else -> {
                    val json = remoteConfig.getString(key)
                    if (json.isNotEmpty()) {
                        moshi.adapter(clazz).fromJson(json)
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            // Log error or handle appropriately
            null
        }
    }

    fun fetchAndActive(onComplete: (isSuccess: Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                onComplete.invoke(it.isSuccessful)
            }
    }

    internal fun getAppConfig(): AppConfigModel? {
        return remoteConfig.read(
            moshi,
            ConfigParam.AppConfig
        )
    }

    internal fun getIapConfig(): IapConfigModel? {
        return remoteConfig.read(
            moshi,
            ConfigParam.IapConfig
        )
    }

}