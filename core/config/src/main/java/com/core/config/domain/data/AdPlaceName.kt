//package com.core.config.domain.data
//
//enum class IAdPlaceName(val key: String) {
//
//    NONE(""),
//
//    ANCHORED_NATIVE_TEST("anchored_native_test"),
//    ANCHORED_BOTTOM_HOME("anchored_bottom_home"),
//    ANCHORED_NATIVE_IN_LIST_TEST("anchored_native_in_list_test"),
//    ANCHORED_BANNER_TEST("anchored_banner_test"),
//    ANCHORED_EXIT("anchored_exit"),
//    FULLSCREEN_TEST("fullscreen_test"),
//    FULLSCREEN_TEST_LAZY_LOAD("fullscreen_test_lazy_load"),
//    REWARD_TEST("reward_test"),
//
//    ANCHORED_MAIN_BOTTOM("anchored_main_bottom"),
//
//    /**Quảng cáo banner or native ở màn onboarding*/
//    ANCHORED_ONBOARDING_BOTTOM("anchored_onboarding_bottom"),
//    ANCHORED_FULL_ONBOARDING("anchored_full_onboarding"),
//
//    /**Quảng cáo banner or native ở màn langague*/
//    ANCHORED_CHANGE_LANGUAGE_BOTTOM("anchored_change_language_bottom"),
//
//    /**Quảng cáo banner or native ở màn langague*/
//    ANCHORED_UNINSTALL_BOTTOM_STEP_1("anchored_uninstall_bottom_step_1"),
//    ANCHORED_UNINSTALL_BOTTOM_STEP_2("anchored_uninstall_bottom_step_2"),
//
//    ANCHORED_SETTING_BOTTOM("anchored_setting_bottom"),
//    ANCHORED_MAIN_CENTER("anchored_main_center"),
//    ACTION_NEXT_IN_INTRODUCTION("action_next_in_introduction"),
//    ACTION_SKIP_IN_INTRODUCTION("action_skip_in_introduction"),
//
//    FULLSCREEN_SETTING_TO_HOME("fullscreen_setting_to_home"),
//    FULLSCREEN_SHOW_TO_HOME("fullscreen_show_to_home"),
//    FULLSCREEN_REWARDED_UNLOCK_BACKGROUND("fullscreen_rewarded_unlock_background"),
//    FULLSCREEN_ASSISTANT_TO_CHAT("fullscreen_assistant_to_chat"),
//
//    ACTION_OPEN_APP_FIRST_OPEN("action_app_open_first_open"),
//    ACTION_OPEN_APP("action_app_open"),
//
//    ACTION_BACK_IN_IAP("action_back_in_iap"),
//
//    APP_OPEN_FIRST_OPEN("open_app_first_open"),
//    APP_OPEN("open_app"),
//    APP_REOPEN("reopen_app");
//
//    companion object {
//
//        fun getAdPlaceBy(key: String): IAdPlaceName {
//            return try {
//                values().forEach {
//                    if (it.key == key) {
//                        return  it
//                    }
//                }
//                return NONE
//            } catch (_: Exception) {
//                NONE
//            }
//        }
//    }
//}
