package com.core.preference

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

@Suppress("UNCHECKED_CAST")
open class SecuritySharedPrefs(private val context: Context, private var _name: String = "security_shared_prefs") {
    private val GSON = Gson()

    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Create an instance of EncryptedSharedPreferences
    val mSharedPreferences = EncryptedSharedPreferences.create(
        context,
        _name, // Filename
        masterKey, // Master key
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


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

        private var mInstance: SecuritySharedPrefs? = null

        fun init(context: Context) {
            mInstance = SecuritySharedPrefs(context)
        }

        val instance: SecuritySharedPrefs
            get() {
                if (mInstance == null) {
                    throw RuntimeException("SecuritySharedPrefs not initialized")
                }
                return mInstance!!
            }
    }
}