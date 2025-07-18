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

    private const val KEY_PROXY = "PROXY"

    var proxy: String
        get() = SharedPrefs.instance[KEY_PROXY, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_PROXY, value)
        }

    private const val DEVICE_ID = "DEVICE_ID"

    var deviceId: String
        get() = SharedPrefs.instance[DEVICE_ID, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(DEVICE_ID, value)
        }

//    private const val KEY_PROXY_TYPE = "PROXY_TYPE"
//
//    var proxyType: String
//        get() = SharedPrefs.instance[KEY_PROXY_TYPE, String::class.java, "http"]
//        set(value) {
//            SharedPrefs.instance.put(KEY_PROXY_TYPE, value)
//        }

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
//
//    private const val KEY_PROXY_PORT = "PROXY_PORT"
//    var proxyPort: String
//        get() = SharedPrefs.instance[KEY_PROXY_PORT, String::class.java, ""]
//        set(value) {
//            SharedPrefs.instance.put(KEY_PROXY_PORT, value)
//        }
//
//    private const val KEY_PROXY_USER = "PROXY_USER"
//    var proxyUser: String
//        get() = SharedPrefs.instance[KEY_PROXY_USER, String::class.java, ""]
//        set(value) {
//            SharedPrefs.instance.put(KEY_PROXY_USER, value)
//        }
//
//    private const val KEY_PROXY_PASS = "PROXY_PASS"
//    var proxyPass: String
//        get() = SharedPrefs.instance[KEY_PROXY_PASS, String::class.java, ""]
//        set(value) {
//            SharedPrefs.instance.put(KEY_PROXY_PASS, value)
//        }

    private const val KEY_PUBLIC_IP = "PUBLIC_IP"

    var publicIP: String
        get() = SharedPrefs.instance[KEY_PUBLIC_IP, String::class.java, "26.25.12.111"]
        set(value) {
            SharedPrefs.instance.put(KEY_PUBLIC_IP, value)
        }

    private const val KEY_APP_INSTALL_ID = "APP_INSTALL_ID"

    var appInstallID: String
        get() = SharedPrefs.instance[KEY_APP_INSTALL_ID, String::class.java, ""]
        set(value) {
            SharedPrefs.instance.put(KEY_APP_INSTALL_ID, value)
        }

    private const val KEY_DNS_SERVER = "DNS_SERVER"
    var dnsServer: String
        get() = SharedPrefs.instance[KEY_DNS_SERVER, String::class.java, "1.1.1.1"]
        set(value) {
            SharedPrefs.instance.put(KEY_DNS_SERVER, value)
        }

    private const val KEY_ADS_BLOCK = "ADS_BLOCK"
    var adsBlock: Boolean
        get() = SharedPrefs.instance[KEY_ADS_BLOCK, Boolean::class.java, false]
        set(value) {
            SharedPrefs.instance.put(KEY_ADS_BLOCK, value)
        }

    private const val DATA_COLLECTION = "DATA_COLLECTION"
    var allowCollectData: Boolean
        get() = SharedPrefs.instance[DATA_COLLECTION, Boolean::class.java, false]
        set(value) {
            SharedPrefs.instance.put(DATA_COLLECTION, value)
        }

    private const val KEY_FEEDBACK = "FEEDBACK"
    var isFeedback: Boolean
        get() = SharedPrefs.instance[KEY_FEEDBACK, Boolean::class.java, false]
        set(value) {
            SharedPrefs.instance.put(KEY_FEEDBACK, value)
        }

    private const val KEY_IS_SUBSCRIPTION = "IS_SUBSCRIPTION"
    var isSub: Boolean
        get() = SharedPrefs.instance[KEY_IS_SUBSCRIPTION, Boolean::class.java, false]
        set(value) {
            SharedPrefs.instance.put(KEY_IS_SUBSCRIPTION, value)
        }
}