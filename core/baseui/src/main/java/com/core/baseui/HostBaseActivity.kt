package com.core.baseui

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.core.baseui.ext.TransitionType
import com.core.baseui.ext.collectFlowOn
import com.core.baseui.ext.handleAdd
import com.core.baseui.ext.handleReplace
import com.core.baseui.navigator.NavigatorEvent
import kotlinx.coroutines.CoroutineScope


abstract class HostBaseActivity<B : ViewBinding, E : NavigatorEvent, VM : BaseSharedViewModel<E>>() :
    BaseActivity<B>(), CoroutineScope {


    abstract val containerId: Int

    abstract val sharedViewModel: VM

    @CallSuper
    open fun onNavigateTo(event: E) {
        sharedViewModel.currentEvent = event
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.isActivityResume = true
        if (sharedViewModel.needHandleEventWhenResume) {
            sharedViewModel.needHandleEventWhenResume = false
            onNavigateTo(sharedViewModel.currentEvent)
        }
    }

    override fun onPause() {
        sharedViewModel.isActivityResume = false
        super.onPause()
    }

    override fun handleObservable() {
        super.handleObservable()
        collectFlowOn(sharedViewModel.navigateToFlow) { event ->
            if (sharedViewModel.isActivityResume) {
                onNavigateTo(event)
            } else {
                sharedViewModel.needHandleEventWhenResume = true
            }
        }
    }

    fun replaceFragment(
        fragment: Fragment,
        transitionType: TransitionType? = null,
        isEnableDuplicateFragment: Boolean = false,
        addToBackStack: Boolean = true,
    ) {
        supportFragmentManager.handleReplace(
            containerId = containerId,
            transitionType = transitionType,
            fragment = fragment,
            isEnableDuplicateFragment = isEnableDuplicateFragment,
            addToBackStack = addToBackStack
        )
    }

    fun addFragment(
        fragment: Fragment,
        transitionType: TransitionType? = null,
        isEnableDuplicateFragment: Boolean = false,
        addToBackStack: Boolean = true,
    ) {
        supportFragmentManager.handleAdd(
            containerId = containerId,
            transitionType = transitionType,
            fragment = fragment,
            isEnableDuplicateFragment = isEnableDuplicateFragment,
            addToBackStack = addToBackStack
        )
    }
}

