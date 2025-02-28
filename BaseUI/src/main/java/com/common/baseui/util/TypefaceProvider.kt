package com.common.baseui.util

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import java.io.File

class TypefaceProvider {
    companion object {
        private var instance = TypefaceProvider()
        private const val TAG = "TypefaceProvider"
        fun getInstance(): TypefaceProvider = instance
    }

    var typefaces = HashMap<String, Typeface>()

    fun getTypeface(context: Context, path: String): Typeface {
        return typefaces[path] ?: run {
            val typeface = try {
                if(File(path).exists()) {
                    Typeface.createFromFile(path)
                } else {
                    Typeface.createFromAsset(context.assets, path)
                }
            } catch (ex: Exception) {
                Log.d(TAG, ex.toString())
                Typeface.DEFAULT
            }
            typefaces[path] = typeface
            typeface
        }
    }

    fun getTypeface(context: Context,@FontRes id: Int): Typeface {
        return typefaces[id.toString()] ?: run {
            val typeface = try {
                ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT
            } catch (ex: Exception) {
                Log.d(TAG, ex.toString())
                Typeface.DEFAULT
            }
            typefaces[id.toString()] = typeface
            typeface
        }
    }

    fun getTypefaceOrNull(context: Context, path: String): Typeface? {
        return typefaces[path] ?: run {
            val typeface = try {
                if(File(path).exists()) {
                    Typeface.createFromFile(path)
                } else {
                    Typeface.createFromAsset(context.assets, path)
                }
            } catch (ex: Exception) {
                Log.d(TAG, ex.toString())
                null
            }
            typeface?.let {
                typefaces[path] = typeface
            }
            typeface
        }
    }
}