package com.core.config.data.helper

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private const val TAG = "RemoteConfigHelper"
internal inline fun <reified T : Any, P : ConfigParam<T>> FirebaseRemoteConfig.read(
    moshi: Moshi,
    param: P,
): T? {
    val value: Any? = when (T::class) {
        String::class -> getString(param.key)
        Int::class -> getLong(param.key).toInt()
        Boolean::class -> getBoolean(param.key)
        Float::class -> getDouble(param.key).toFloat()
        Double::class -> getDouble(param.key)
        Long::class -> getLong(param.key)
        else -> {
            try {
                val json = getString(param.key)
                json.takeIf {
                    it.isNotBlank()
                }?.let {
                    val jsonAdapter = moshi.adapter(T::class.java)
                    jsonAdapter.fromJson(json)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    return value as? T
}


internal inline fun <reified T : Any, P : ConfigParam<T>> FirebaseRemoteConfig.readList(
    moshi: Moshi,
    param: P,
): List<T> {
    try {
        val json = getString(param.key)
        Log.d(TAG, "readList: $json")
        json.takeIf {
            it.isNotBlank()
        }?.let {
            val listType = Types.newParameterizedType(List::class.java, T::class.java)
            val jsonAdapter: JsonAdapter<List<T>> = moshi.adapter(listType)
            return jsonAdapter.fromJson(json) ?: listOf()
        }
    } catch (e: Exception) {
        Log.d(TAG, "readList: Failed ${param.key} $e")
        return listOf()
    }
    return listOf()
}
