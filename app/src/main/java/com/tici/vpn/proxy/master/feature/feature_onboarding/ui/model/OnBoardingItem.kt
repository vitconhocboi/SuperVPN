package com.tici.vpn.proxy.master.feature.feature_onboarding.ui.model

import com.core.config.domain.data.AppConfig
import com.tici.vpn.proxy.master.feature.feature_onboarding.ui.helper.OnBoardingConfigFactory

/** Một trang nội dung onboarding. [position] là index ảnh/tiêu đề trong OnBoardingConfigFactory. */
class OnBoardingItem(
    val position: Int,
    var isPageEnd: Boolean = false
) {
    companion object {
        /**
         * Dựng danh sách trang từ cấu hình remote (introData/introDataV2).
         * Entry quảng cáo full-screen bị bỏ qua, chỉ giữ trang nội dung.
         * Nếu cấu hình rỗng → dùng [OnBoardingConfigFactory.INTRO_PAGE_COUNT] trang mặc định.
         */
        fun fromIntroData(introData: List<Int>): List<OnBoardingItem> {
            val pageCount = introData.count { it != AppConfig.DEFINE_INTRO_FULL_AD }
                .takeIf { it > 0 } ?: OnBoardingConfigFactory.INTRO_PAGE_COUNT
            return List(pageCount) { index ->
                OnBoardingItem(position = index, isPageEnd = index == pageCount - 1)
            }
        }
    }
}