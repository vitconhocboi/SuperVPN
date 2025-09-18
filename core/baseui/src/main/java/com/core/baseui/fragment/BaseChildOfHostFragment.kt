package com.core.baseui.fragment

import android.content.res.Configuration
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.core.baseui.BaseSharedViewModel
import com.core.baseui.navigator.NavigatorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseChildOfHostFragment<B: ViewBinding, E : NavigatorEvent, HVM : BaseSharedViewModel<E>>(
) : BaseFragment<B>(), CoroutineScope {

    companion object {
        const val TAG = "Fragment"
    }


    override val isHandleBackPress = true

    override fun handleOnBackPressed() {
        hostViewModel.navigateActionBack()
    }

    abstract val hostViewModel: HVM

    override fun onStart() {
        Glide.with(this).onStart()
        super.onStart()
        Log.i(TAG, "$screenType onStart")
//        preloadAds()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "$screenType onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "$screenType onPause")
    }

    override fun onStop() {
        Glide.with(this).onStop()
        super.onStop()
        Log.i(TAG, "$screenType onStop")
    }

    override fun onDestroyView() {
        coroutineContext.cancelChildren()
        try {
            Glide.with(this).onDestroy()
        } catch (_: Exception) {
        }
        super.onDestroyView()
        Log.i(TAG, "$screenType onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "$screenType onDestroy")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Glide.with(this).onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    fun navigateTo(event: E) {
        hostViewModel.navigateTo(event)
    }

}

fun <T : Any?> Fragment.collectFlowOn(
    sharedFlow: SharedFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    onResult: (t: T) -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
            sharedFlow.collectLatest {
                onResult.invoke(it)
            }
        }
    }
}

fun <T : Any?> Fragment.collectFlowOn(
    stateFlow: StateFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    onResult: (t: T) -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}