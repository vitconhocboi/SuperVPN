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
}