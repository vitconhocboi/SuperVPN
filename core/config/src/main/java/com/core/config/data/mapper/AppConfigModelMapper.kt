package com.core.config.data.mapper

import com.core.config.data.model.AppConfigModel
import com.core.config.domain.data.AppConfig
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_HAVE_ADS
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_NO_ADS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AppConfigModelMapper @Inject constructor(
) : ModelMapper<AppConfigModel, AppConfig> {

    override fun toData(model: AppConfigModel): AppConfig {
        return AppConfig(
            isHideNavigationBar = model.isHideNavigationBar ?: false,
            isAlwaysShowIntroAndLanguageScreen = model.isAlwaysShowIntroAndLanguageScreen ?: false,
            isAlwaysShowIntroAndLanguageScreenWithInterval = model.isAlwaysShowIntroAndLanguageScreenWithInterval ?: false,
            isEnableIntroductionScreen = model.isEnableIntroductionScreen ?: true,
            isEnableChangeLanguageScreen = model.isEnableChangeLanguageScreen ?: true,
            isEnableAppShortCut = model.isEnableAppShortCut ?: false,
            isEnableAppShortcutUninstall = model.isEnableAppShortcutUninstall ?: false,
            introActionShowType = model.introActionShowType ?: 1,
            introData = model.introData ?: arrayListOf(
                DEFINE_INTRO_HAVE_ADS,
                DEFINE_INTRO_HAVE_ADS,
                DEFINE_INTRO_HAVE_ADS
            ), introDataV2 = model.intro_data_v2 ?: arrayListOf(
                DEFINE_INTRO_NO_ADS,
                DEFINE_INTRO_NO_ADS,
                DEFINE_INTRO_HAVE_ADS
            ),
            intervalDayAlwaysShowIntroAndLanguage = model.intervalDayAlwaysShowIntroAndLanguage ?: 3,
        )
    }

}