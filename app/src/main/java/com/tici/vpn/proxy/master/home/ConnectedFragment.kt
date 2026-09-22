package com.tici.vpn.proxy.master.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.common.baseui.BaseAppConfig
import com.common.baseui.extension.context
import com.core.baseui.fragment.BaseFragment
import com.core.baseui.fragment.ScreenType
import com.core.rate.RateInApp
import com.core.utilities.util.Timber
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.tici.vpn.proxy.master.R
import com.tici.vpn.proxy.master.databinding.FragmentConnectedBinding
import com.tici.vpn.proxy.master.required.preferences.CoreAppPreferences
import com.tici.vpn.proxy.master.required.shortcut.AppScreenType
import com.tici.vpn.proxy.master.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectedFragment : BaseFragment<FragmentConnectedBinding>() {

    override val screenType: ScreenType
        get() = AppScreenType.ConnectedFragment

    override fun bindingProvider(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentConnectedBinding {
        return FragmentConnectedBinding.inflate(inflater, container, false)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        // No server to show: the tunnel is local-only.
    }

    fun Context.safeGetString(resourceName: String): String? {
        val resId = resources.getIdentifier(resourceName, "string", packageName)
        return if (resId != 0) getString(resId) else null
    }

//    override fun getTitle(): String {
//        return try {
//            resources.getString(R.string.connect_success)
//        } catch (e: Exception) {
//            resources.getString(R.string.connect_success)
//        }
//    }

}