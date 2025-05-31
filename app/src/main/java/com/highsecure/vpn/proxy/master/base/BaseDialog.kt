package com.highsecure.vpn.proxy.master.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.highsecure.vpn.proxy.master.extension.dp2Px

abstract class BaseDialog<VB: ViewBinding>(val activity: FragmentActivity) : Dialog(activity) {

    private val mHandler = Handler(Looper.getMainLooper())
    protected var binding: VB? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val view = currentFocus

            if (view is AppCompatEditText || view is EditText) {
                val scrcoords = IntArray(2)
                view.getLocationOnScreen(scrcoords)
                val x = event.rawX
                val y = event.rawY

                if (x < scrcoords[0] || x > scrcoords[0] + view.measuredWidth
                    || y < scrcoords[1] || y > scrcoords[1] + view.measuredHeight
                ) {
                    mHandler.removeCallbacksAndMessages(null)
                    mHandler.postDelayed({
                        val parentView = view.parent?.parent
                        if ((currentFocus !is AppCompatEditText && currentFocus !is EditText)
                            || (view == currentFocus)
                        ) {
                            //do something
                            hide()
                        }
                    }, 150)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = initViewBinding(activity)
        setContentView(binding!!.root)
        initUI()
        setUpMargin()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
        mHandler.removeCallbacksAndMessages(null)
    }

    abstract fun initUI()
    abstract fun initViewBinding(activity: Activity): VB

    private fun setUpMargin() {
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            val windowParams = attributes
            windowParams.horizontalMargin = activity.dp2Px(32).toFloat()

            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            attributes = windowParams
            setGravity(Gravity.CENTER)
        }
    }
}