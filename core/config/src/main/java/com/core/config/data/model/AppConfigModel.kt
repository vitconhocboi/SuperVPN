package com.core.config.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppConfigModel(

    @Json(name = "is_hide_navigation_bar")
    val isHideNavigationBar: Boolean?,

    @Json(name = "is_always_show_intro_and_language_screen")
    val isAlwaysShowIntroAndLanguageScreen : Boolean?,

    @Json(name = "is_always_show_intro_and_language_screen_with_interval")
    val isAlwaysShowIntroAndLanguageScreenWithInterval : Boolean?,

    @Json(name = "is_enable_introduction_screen")
    val isEnableIntroductionScreen : Boolean?,

    @Json(name = "is_enable_change_language_screen")
    val isEnableChangeLanguageScreen : Boolean?,

    @Json(name = "is_enable_app_shortcut")
    val isEnableAppShortCut : Boolean?,

    @Json(name = "is_enable_app_shortcut_uninstall")
    val isEnableAppShortcutUninstall : Boolean?,

    @Json(name = "intro_action_show_type")
    val introActionShowType: Int?,

    @Json(name = "interval_day_always_show_intro_and_language")
    val intervalDayAlwaysShowIntroAndLanguage: Int?,

    /**<!--intro data-->
        <!--0 = không có ads-->
        <!--1 = Có ads-->
        <!--2 = Full ads-->
     */
    @Json(name = "intro_data")
    val introData: List<Int>?,
    @Json(name = "intro_data_v2")
    val intro_data_v2: List<Int>?

)