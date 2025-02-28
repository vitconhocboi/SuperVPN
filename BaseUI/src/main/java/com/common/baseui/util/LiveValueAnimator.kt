package com.common.baseui.util

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import androidx.lifecycle.LiveData
import com.common.baseui.SingleLiveCallBack

class LiveValueAnimator(var from: Float, var to: Float, var duration: Long) {
    private var _liveData: SingleLiveCallBack<Data> = SingleLiveCallBack()
    private var _animator: ValueAnimator = ValueAnimator.ofFloat(from, to).apply {
        setDuration(duration)
    }

    fun liveProgress(): LiveData<Data> {
        return _liveData
    }

    fun run() {
        _animator.setDuration(1500)
        _animator.addUpdateListener { animation ->
            _liveData.value = Data(state = State.UPDATE, animation.getAnimatedValue() as Float)
        }
        _animator.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                _liveData.value = Data(state = State.END, value = to)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

        })
        _animator.start()
    }

    fun cancel() {
        _animator.cancel()
    }

    enum class State {
        UPDATE,
        END
    }
    class Data (
        var state: State,
        var value: Float
    )
}