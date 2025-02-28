package com.common.baseui.extension

import android.graphics.Matrix
import android.util.Log
import kotlin.math.atan2
import kotlin.math.sqrt

inline val Matrix.rotation: Float
    get() {
        val v = FloatArray(9)
        getValues(v)
        return atan2(
            v[Matrix.MSKEW_X],
            v[Matrix.MSCALE_X]
        ) * (180f / Math.PI.toFloat())
    }

fun Matrix.getTransX(): Float {
    val v = FloatArray(9)
    getValues(v)
    return v[Matrix.MTRANS_X]
}

fun Matrix.getTransY(): Float {
    val v = FloatArray(9)
    getValues(v)
    return v[Matrix.MTRANS_Y]
}

fun Matrix.resetTranslate() {
    val translateX = getTransX()
    val translateY = getTransY()
    postTranslate(-translateX, -translateY)
}

fun Matrix.resetScale() {
    val scale = getRealScaleX()
    postTranslate(1/scale, 1/scale)
}

fun Matrix.getRealScaleX(): Float {
    // calculate real scale
    val v = FloatArray(9)
    getValues(v)
    val scalex = v[Matrix.MSCALE_X]
    val skewy = v[Matrix.MSKEW_Y]
    val rScale =
        sqrt(scalex * scalex + skewy * skewy.toDouble()).toFloat()
    Log.d("getRealScale", "getRealScale: $rScale")
    return rScale
}

fun Matrix.getScaleX(): Float {
    // calculate real scale
    val v = FloatArray(9)
    getValues(v)
    val scaleX = v[Matrix.MSCALE_X]
    return scaleX
}

fun Matrix.getScaleY(): Float {
    // calculate real scale
    val v = FloatArray(9)
    getValues(v)
    val scaleY = v[Matrix.MSCALE_Y]
    return scaleY
}

fun Matrix.getRealScaleY(): Float {
    // calculate real scale
    val v = FloatArray(9)
    getValues(v)
    val scaleY = v[Matrix.MSCALE_Y]
    val skewX = v[Matrix.MSKEW_X]
    val rScale =
        sqrt(scaleY * scaleY + skewX * skewX.toDouble()).toFloat()
    Log.d("getRealScale", "getRealScale: $rScale")
    return rScale
}
fun Matrix.logString(): String {
    // calculate real scale
    return "ScaleX ${getScaleX()} : ${getScaleY()} RealScale ${getRealScaleX()}:${getRealScaleY()} Rotate $rotation Translate ${getTransX()} : ${getTransY()}"
}
