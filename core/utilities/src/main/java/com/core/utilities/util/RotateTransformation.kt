package com.core.utilities.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

class RotateTransformation(private val angle: Float) : BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("rotate$angle").toByteArray(Charset.forName("UTF-8")))
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val matrix = Matrix().apply { postRotate(angle) }
        val rotatedBitmap = Bitmap.createBitmap(toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true)
        return rotatedBitmap
    }

    override fun equals(other: Any?): Boolean {
        return other is RotateTransformation && other.angle == angle
    }

    override fun hashCode(): Int {
        return angle.hashCode()
    }
}
