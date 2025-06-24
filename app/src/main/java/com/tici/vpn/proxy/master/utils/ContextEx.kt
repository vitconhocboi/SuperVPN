package com.tici.vpn.proxy.master.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import com.tici.vpn.proxy.master.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun Context.toast(@StringRes resId: Int, toastLength: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, resId, toastLength).show()

fun Context.toast(text: String, toastLength: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, toastLength).show()

fun Context.shareApp() {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            String.format(
                "%s: %s", getString(R.string.app_name), "https://play.google" +
                        ".com/store/apps/details?id=$packageName"
            )
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        toast(R.string.msg_device_does_not_support_this_feature)
    }
}

@OptIn(FlowPreview::class)
@ExperimentalCoroutinesApi
fun EditText.textChanges(scope: CoroutineScope, onChange: (String) -> Unit) {
    val searchChannel = MutableStateFlow("")
    onTextChanged {
//        Timber.d("textChanges emit $it")
        searchChannel.tryEmit(it)
    }
    searchChannel.asStateFlow()
        .debounce(300)
        .onEach {
            onChange(it)
        }.launchIn(scope)
//    Timber.d("textChanges first emit ${text?.toString()}")
    searchChannel.tryEmit(text?.toString() ?: "")
}

fun EditText.onTextChanged(listener: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {}
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            listener(s.toString())
        }
    })
}

fun View.hideKeyboard() {
    clearFocus()
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}