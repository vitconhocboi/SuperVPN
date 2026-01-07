package com.tici.vpn.proxy.master.required.ads

import android.util.Log
import com.core.config.domain.data.IAdPlaceName

private const val TAG = "AppAdPlaceName"

sealed class AppAdPlaceName(override val name: String) : IAdPlaceName {

    /**Tạo mới cần add thêm vào list APP_AD_PLACE_LIST bên dưới*/

    object ANCHORED_NATIVE_TEST : AppAdPlaceName("anchored_native_test")
    object ANCHORED_BOTTOM_HOME : AppAdPlaceName("anchored_bottom_home")
    object ANCHORED_NATIVE_IN_LIST_TEST : AppAdPlaceName("anchored_native_in_list_test")
    object ANCHORED_BANNER_TEST : AppAdPlaceName("anchored_banner_test")
    object ANCHORED_EXIT : AppAdPlaceName("anchored_exit")
    object FULLSCREEN_TEST : AppAdPlaceName("fullscreen_test")
    object FULLSCREEN_TEST_LAZY_LOAD : AppAdPlaceName("fullscreen_test_lazy_load")
    object REWARD_TEST : AppAdPlaceName("reward_test")

    object ANCHORED_CENTER_HOME : AppAdPlaceName("anchored_center_home")

    object ANCHORED_TOP_CONFIRM_DISCONNECT : AppAdPlaceName("anchored_top_confirm_disconnect")

    object ANCHORED_BOTTOM_CONNECTED_SUCCESS : AppAdPlaceName("anchored_bottom_connected_success")

    object ANCHORED_BOTTOM_CONNECTED_REPORT : AppAdPlaceName("anchored_bottom_connected_report")

    object ANCHORED_BOTTOM_SETTINGS : AppAdPlaceName("anchored_bottom_settings")

    object ANCHORED_BOTTOM_DNS_DEFAULT : AppAdPlaceName("anchored_bottom_dns_default")

    object ANCHORED_BOTTOM_APP_PROXY : AppAdPlaceName("anchored_bottom_app_proxy")

    object ANCHORED_BOTTOM_VPN_SEVERS : AppAdPlaceName("anchored_bottom_vpn_severs")

    object ANCHORED_BOTTOM_MY_IP : AppAdPlaceName("anchored_bottom_my_ip")

    object ANCHORED_BOTTOM_EXIT : AppAdPlaceName("anchored_bottom_exit")

    object FULLSCREEN_SELECTED_PROXY_HOME : AppAdPlaceName("fullscreen_selected_proxy_home")

    object REWARDED_CONNECT_VPN : AppAdPlaceName("rewarded_connect_vpn")

    object FULLSCREEN_BACK_DISCONNECT : AppAdPlaceName("fullscreen_back_disconnect")

    object FULLSCREEN_BACK_MAIN: AppAdPlaceName("fullscreen_back_main")

    object FULLSCREEN_BACK_TAB_PROXY: AppAdPlaceName("fullscreen_back_tab_proxy")

    object FULLSCREEN_CONNECTED_PROXY: AppAdPlaceName("fullscreen_connected_proxy")

    object FULLSCREEN_CONNECTED_PROXY_GAME: AppAdPlaceName("fullscreen_connected_proxy_game")

    object FULLSCREEN_SELECTED_CHANGE_VPN_SERVERS :
        AppAdPlaceName("fullscreen_selected_change_vpn_servers")

    companion object {
        /**Cần add thêm vào đây nếu tạo thêm AdPlaceName*/
        val APP_AD_PLACE_LIST: List<AppAdPlaceName> by lazy {
            AppAdPlaceName::class.sealedSubclasses.mapNotNull { it.objectInstance }
        }

        // Hàm lấy ad theo key string
        fun fromKey(key: String): AppAdPlaceName? {
            Log.d(TAG, "fromKey: ${APP_AD_PLACE_LIST}")

            return APP_AD_PLACE_LIST.find {
                it.name == key
            }
        }
    }
}