package com.common.baseui

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson


@Suppress("UNCHECKED_CAST")
open class SharedPrefs(private var _name: String = "com.common.baseui") {
    private val GSON = Gson()
    private val mSharedPreferences: SharedPreferences =
        BaseApplication.instance.getSharedPreferences(_name, Context.MODE_PRIVATE)


    init {

    }


    @SuppressWarnings("unchecked")
    operator fun <T> get(key: String, anonymousClass: Class<T>): T? {
        return when (anonymousClass) {
            String::class.java -> mSharedPreferences.getString(key, "") as T
            Boolean::class.java -> java.lang.Boolean.valueOf(
                mSharedPreferences.getBoolean(
                    key,
                    false
                )
            ) as T
            Float::class.java -> java.lang.Float.valueOf(mSharedPreferences.getFloat(key, 0f)) as T
            Double::class.java -> java.lang.Double.valueOf(mSharedPreferences.getFloat(key, 0f).toDouble()) as T
            Int::class.java -> Integer.valueOf(mSharedPreferences.getInt(key, 0)) as T
            Long::class.java -> java.lang.Long.valueOf(mSharedPreferences.getLong(key, 0)) as T
            else -> {
                if (mSharedPreferences.getString(key, null) != null) {
                    GSON.fromJson(
                        mSharedPreferences.getString(key, ""),
                        anonymousClass
                    )
                } else null
            }
        }
    }

    @SuppressWarnings("unchecked")
    operator fun <T> get(key: String, anonymousClass: Class<T>, defaultValue: T): T {
        return when (anonymousClass) {
            String::class.java -> mSharedPreferences.getString(key, defaultValue as String) as T
            Boolean::class.java -> java.lang.Boolean.valueOf(
                mSharedPreferences.getBoolean(
                    key,
                    defaultValue as Boolean
                )
            ) as T
            Float::class.java -> java.lang.Float.valueOf(
                mSharedPreferences.getFloat(
                    key,
                    defaultValue as Float
                )
            ) as T

            Double::class.java -> java.lang.Double.valueOf(
                mSharedPreferences.getFloat(
                    key,
                    defaultValue as Float
                ).toDouble()
            ) as T
            Int::class.java -> Integer.valueOf(
                mSharedPreferences.getInt(
                    key,
                    defaultValue as Int
                )
            ) as T
            Long::class.java -> java.lang.Long.valueOf(
                mSharedPreferences.getLong(
                    key,
                    defaultValue as Long
                )
            ) as T
            else -> {
                if (mSharedPreferences.getString(key, null) != null) {
                    GSON.fromJson(
                        mSharedPreferences.getString(key, ""),
                        anonymousClass
                    )
                } else defaultValue
            }
        }
    }

    fun <T> put(key: String, data: T?) {
        val editor = mSharedPreferences.edit()
        when (data) {
            is String -> editor.putString(key, data as String)
            is Boolean -> editor.putBoolean(key, data as Boolean)
            is Float -> editor.putFloat(key, data as Float)
            is Int -> editor.putInt(key, data as Int)
            is Long -> editor.putLong(key, data as Long)
            (data == null) -> editor.remove(key)
            else -> editor.putString(key, GSON.toJson(data))
        }
        editor.apply()
    }

    fun clear() {
        mSharedPreferences.edit().clear().apply()
    }

    companion object {

        private var mInstance: SharedPrefs? = null

        val instance: SharedPrefs
            get() {
                if (mInstance == null) {
                    mInstance = SharedPrefs()
                }
                return mInstance!!
            }
    }
}