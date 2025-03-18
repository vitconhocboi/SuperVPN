package com.common.baseui

object BaseAppConfig {
    private const val KEY_LANGUAGE_CODE = "LANGUAGE_CODE"
    private const val KEY_FIRST_TIME_SETUP = "KEY_FIRST_TIME_SETUP"

    var languageCode: String
        get() = SharedPrefs.instance[KEY_LANGUAGE_CODE, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_LANGUAGE_CODE, value)
        }

    var firstTimeSetup: Boolean
        get() = SharedPrefs.instance[KEY_FIRST_TIME_SETUP, Boolean::class.java, true]
        set(value) {
            SharedPrefs.instance.put(KEY_FIRST_TIME_SETUP, value)
        }
    private const val KEY_PROXY_ADDRESS = "PROXY_ADDRESS"

    var proxyAddress: String
        get() = "http://192.168.1.102:8080"
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_ADDRESS, value)
        }

    private const val KEY_PUBLIC_IP = "PUBLIC_IP"

    var publicIP: String
        get() = SharedPrefs.instance[KEY_PUBLIC_IP, String::class.java, "26.25.12.132"]
        set(value) {
            SharedPrefs.instance.put(KEY_PUBLIC_IP, value)
        }

    private const val KEY_APP_INSTALL_ID = "APP_INSTALL_ID"

    var appInstallID: String
        get() = SharedPrefs.instance[KEY_APP_INSTALL_ID, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_APP_INSTALL_ID, value)
        }
}