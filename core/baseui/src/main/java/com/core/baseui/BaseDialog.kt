package com.core.baseui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

abstract class BaseDialog(context: Context) : Dialog(context), CoroutineScope {

    private val job = Job()

    var isHideNavigationBar = false

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        const val TABLET_RATIO_WIDTH_DIALOG = 0.85f
        const val PHONE_RATIO_WIDTH_DIALOG = 0.8f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView()
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(isCancelable)
        initView()
        if (isHideNavigationBar) {
            hideNavigationBar()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isHideNavigationBar) {
            hideNavigationBar()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineContext.cancelChildren()
    }

    override fun dismiss() {
        super.dismiss()
        coroutineContext.cancelChildren()
    }

    protected abstract val isCancelable: Boolean

    protected abstract fun setContentView()
    protected abstract fun initView()

    private fun hideNavigationBar() {
        WindowInsetsControllerCompat(
            window!!,
            window!!.decorView
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
