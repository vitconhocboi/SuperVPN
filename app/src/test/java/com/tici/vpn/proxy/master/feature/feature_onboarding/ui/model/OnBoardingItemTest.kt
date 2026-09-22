package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.model

import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_FULL_AD
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_HAVE_ADS
import com.core.config.domain.data.AppConfig.Companion.DEFINE_INTRO_NO_ADS
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnBoardingItemTest {

    @Test
    fun contentPagesOnly_keepsOrderAndMarksLastPageEnd() {
        val items = OnBoardingItem.fromIntroData(
            listOf(DEFINE_INTRO_HAVE_ADS, DEFINE_INTRO_NO_ADS, DEFINE_INTRO_HAVE_ADS)
        )

        assertEquals(listOf(0, 1, 2), items.map { it.position })
        assertEquals(listOf(false, false, true), items.map { it.isPageEnd })
    }

    @Test
    fun fullAdEntries_areFilteredWithoutIndexDrift() {
        val items = OnBoardingItem.fromIntroData(
            listOf(DEFINE_INTRO_HAVE_ADS, DEFINE_INTRO_FULL_AD, DEFINE_INTRO_NO_ADS, DEFINE_INTRO_FULL_AD, DEFINE_INTRO_HAVE_ADS)
        )

        assertEquals(listOf(0, 1, 2), items.map { it.position })
        assertTrue(items.last().isPageEnd)
        assertEquals(1, items.count { it.isPageEnd })
    }

    @Test
    fun emptyConfig_fallsBackToDefaultPageCount() {
        val items = OnBoardingItem.fromIntroData(emptyList())

        assertEquals(OnBoardingConfigFactory.INTRO_PAGE_COUNT, items.size)
        assertTrue(items.last().isPageEnd)
    }

    @Test
    fun onlyFullAdEntries_fallsBackToDefaultPageCount() {
        val items = OnBoardingItem.fromIntroData(listOf(DEFINE_INTRO_FULL_AD, DEFINE_INTRO_FULL_AD))

        assertEquals(OnBoardingConfigFactory.INTRO_PAGE_COUNT, items.size)
        assertEquals(listOf(0, 1, 2), items.map { it.position })
    }
}