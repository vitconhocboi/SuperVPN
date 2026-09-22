package com.common.baseui.extension

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.common.baseui.R
import timber.log.Timber

val Context.inflater get() = LayoutInflater.from(this)

fun Context.showKeyBoard(view: View) {
    if (view.requestFocus()) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }
}

fun Context.hideKeyBoard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.screenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

fun Resources.screenWidth(): Int {
    return displayMetrics.widthPixels
}

fun Resources.screenHeight(): Int {
    return displayMetrics.heightPixels
}

fun Context.copyText(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.pasteText(): String? {
    return try {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.text
        return if(text.isNullOrEmpty()) {
            null
        } else {
            text.toString()
        }
    } catch (ex: Exception) {
        null
    }
}

fun Context.getTextFromAsset(fileName: String): String {
    return this.assets.open(fileName).bufferedReader().use { it.readText() }
}

fun Context.shareApp(message: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        var shareMessage = "$message\n"
        shareMessage += "https://play.google.com/store/apps/details?id=${this.packageName} \n\n"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "choose one"))
    } catch (e: Exception) {
        Timber.e(e.message)
    }
}

const val PERMISSION_WRITE_STORAGE = 1
const val PERMISSION_CAMERA = 2
const val PERMISSION_ALL = 3
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_WRITE_STORAGE -> {
        if (isTiramisuPlus()) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    PERMISSION_CAMERA -> arrayOf(Manifest.permission.CAMERA)

    PERMISSION_ALL -> arrayOf(
        Manifest.permission.CAMERA,
        if (isTiramisuPlus()) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    )
    else -> arrayOf()
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR))
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo != null && networkInfo.isConnected
    }
}

tailrec fun Context.getActivity(): AppCompatActivity? = this as? AppCompatActivity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

fun View.getActivity() = context.getActivity()

