package com.core.config.domain.data


interface IAdPlaceName {
    val name: String
}

private const val TAG = "IAdPlaceName"
sealed class CoreAdPlaceName(
    override val name: String
) : IAdPlaceName {
    // ------- Danh sách các ad place -------
    object NONE : CoreAdPlaceName("")

    object ACTION_NEXT_IN_INTRODUCTION : CoreAdPlaceName("action_next_in_introduction")
    object ACTION_SKIP_IN_INTRODUCTION : CoreAdPlaceName("action_skip_in_introduction")

    object ANCHORED_ONBOARDING_BOTTOM : CoreAdPlaceName("anchored_onboarding_bottom")
    object ANCHORED_ONBOARDING_BOTTOM_v2 : CoreAdPlaceName("anchored_onboarding_bottom_v2")
    object ANCHORED_FULL_ONBOARDING : CoreAdPlaceName("anchored_full_onboarding")
    object ANCHORED_FULL_ONBOARDING_v2 : CoreAdPlaceName("anchored_full_onboarding_v2")

    object ANCHORED_CHANGE_LANGUAGE_BOTTOM : CoreAdPlaceName("anchored_change_language_bottom")
    object ANCHORED_UNINSTALL_BOTTOM_STEP_1 : CoreAdPlaceName("anchored_uninstall_bottom_step_1")
    object ANCHORED_UNINSTALL_BOTTOM_STEP_2 : CoreAdPlaceName("anchored_uninstall_bottom_step_2")

    object ACTION_OPEN_APP_FIRST_OPEN : CoreAdPlaceName("action_app_open_first_open")
    object ACTION_OPEN_APP : CoreAdPlaceName("action_app_open")
    object APP_OPEN_FIRST_OPEN : CoreAdPlaceName("open_app_first_open")
    object APP_OPEN : CoreAdPlaceName("open_app")
    object APP_REOPEN : CoreAdPlaceName("reopen_app")

    companion object {
        // Tự động lấy danh sách tất cả instance
        val ALL: List<CoreAdPlaceName> by lazy {
            CoreAdPlaceName::class.sealedSubclasses.mapNotNull { it.objectInstance }
        }

        // Hàm lấy ad theo key string
        fun fromKey(key: String): CoreAdPlaceName {
            return ALL.find { it.name == key } ?: NONE
        }
    }
}