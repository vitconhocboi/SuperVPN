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

    private const val KEY_PROXY_TYPE = "PROXY_TYPE"

    var proxyType: String
        get() = SharedPrefs.instance[KEY_PROXY_TYPE, String::class.java, "http"]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_TYPE, value)
        }

    private const val KEY_PROXY_COUNTRY = "PROXY_COUNTRY"
    var proxyCountry: String
        get() = SharedPrefs.instance[KEY_PROXY_COUNTRY, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_COUNTRY, value)
        }

    private const val KEY_PROXY_HOST = "PROXY_HOST"
    var proxyHost: String
        get() = SharedPrefs.instance[KEY_PROXY_HOST, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_HOST, value)
        }

    private const val KEY_PROXY_PORT = "PROXY_PORT"
    var proxyPort: String
        get() = SharedPrefs.instance[KEY_PROXY_PORT, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_PORT, value)
        }

    private const val KEY_PROXY_USER = "PROXY_USER"
    var proxyUser: String
        get() = SharedPrefs.instance[KEY_PROXY_USER, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_USER, value)
        }

    private const val KEY_PROXY_PASS = "PROXY_PASS"
    var proxyPass: String
        get() = SharedPrefs.instance[KEY_PROXY_PASS, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY_PASS, value)
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