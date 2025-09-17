package com.tici.vpn.proxy.master.required.shortcut

import com.core.baseui.fragment.ScreenType

sealed class AppScreenType(override val screenName: String) : ScreenType {
    object Uninstall : AppScreenType("Uninstall")

    object ReasonUninstall : AppScreenType("ReasonUninstall")

    object OnBoarding : AppScreenType("OnBoarding")

    object Main : AppScreenType("Main")
    object SettingFragment : AppScreenType("SettingFragment")
    object NativeInList : AppScreenType("NativeInList")

    object BannerNative : AppScreenType("BannerAndNative")

    object Screen1 : AppScreenType("Screen1")
    object Screen2 : AppScreenType("Screen2")

    object Screen3 : AppScreenType("Screen3")

    object None : AppScreenType("")
    object Shop : AppScreenType("Shop")

    data object HomeFragment : AppScreenType("HomeFragment")

    data object ConnectedFragment : AppScreenType("ConnectedFragment")

    data object DisconnectedFragment : AppScreenType("DisconnectedFragment")

    data object IpInfoFragment : AppScreenType("IpInfoFragment")

    data object PremiumFragment : AppScreenType("PremiumFragment")

    data object FreeProxyFragment : AppScreenType("FreeProxyFragment")

    data object TabProxyFragment : AppScreenType("TabProxyFragment")

    data object PremiumProxyFragment : AppScreenType("PremiumProxyFragment")

    data object DNSFragment : AppScreenType("DNSFragment")

    data object AppProxyFragment : AppScreenType("AppProxyFragment")

}