package com.core.baseui.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.core.baseui.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


enum class TransitionType {
    SIBLING, DETAIL, MODAL
}

inline fun <reified T : Fragment> FragmentManager.handleReplace(
    containerId: Int,
    transitionType: TransitionType? = null,
    fragment: T,
    isEnableDuplicateFragment: Boolean,
    addToBackStack: Boolean,
) {
    val tag: String = fragment::class.java.name
    val f = findFragmentByTag(tag)
    if (f != null && !isEnableDuplicateFragment) {
        popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    beginTransaction().apply {
        transitionType?.let {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
//                R.anim.fade_in,
                0,
                R.anim.slide_out
            )
        }
        setReorderingAllowed(true)
        replace(containerId, fragment, tag)
        if (addToBackStack) {
            addToBackStack(tag)
        }
        commitAllowingStateLoss()
    }
}

inline fun <reified T : Fragment> FragmentManager.handleAdd(
    containerId: Int,
    transitionType: TransitionType? = null,
    fragment: T,
    isEnableDuplicateFragment: Boolean,
    addToBackStack: Boolean
) {
    val tag: String = fragment::class.java.name

    val f = findFragmentByTag(tag)
    if (f != null && !isEnableDuplicateFragment) {
        popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    beginTransaction().apply {
        transitionType?.let {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                0,
                R.anim.slide_out
            )
        }
        setReorderingAllowed(true)
        add(containerId, fragment, tag)
        if (addToBackStack) {
            addToBackStack(tag)
        }
        commitAllowingStateLoss()
    }
}


fun FragmentTransaction.setTransition(transitionType: TransitionType) {
    setCustomAnimations(
        when (transitionType) {
            TransitionType.SIBLING -> R.anim.fade_in
            TransitionType.DETAIL -> R.anim.slide_in
            TransitionType.MODAL -> R.anim.slide_in
        },
        R.anim.fade_out,
        R.anim.fade_in,
        when (transitionType) {
            TransitionType.SIBLING -> R.anim.fade_out
            TransitionType.DETAIL -> R.anim.slide_out
            TransitionType.MODAL -> R.anim.slide_out
        }
    )
}



inline fun <T> Fragment.bindFlow(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindFlowResume(
    source: Flow<T>,
    crossinline action: (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindFlowCreate(
    source: Flow<T>,
    crossinline action: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            source.collect { action.invoke(it) }
        }
    }
}

inline fun <T> Fragment.bindLiveData(
    source: LiveData<T>,
    crossinline action: (T) -> Unit
) {
    source.observe(this) {
        action.invoke(it)
    }
}

inline fun <T> bindLiveData(
    source: LiveData<T>,
    lifecycleOwner: LifecycleOwner,
    crossinline action: (T) -> Unit
) {
    source.observe(lifecycleOwner) {
        action.invoke(it)
    }
}
