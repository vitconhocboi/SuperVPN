package com.core.ads.domain

import com.core.config.domain.data.AdPlace
import com.core.config.domain.data.IAdPlaceName


sealed class AdFullScreenUiResource(val rootAdPlaceName: IAdPlaceName) {

    data class AdRequestInfo(val adPlace: AdPlace) : AdFullScreenUiResource(adPlace.placeName)

    data class AdLoaded(val adPlaceName: IAdPlaceName) : AdFullScreenUiResource(adPlaceName)

    data class AdNotValidOrLoadFailed(val adPlaceName: IAdPlaceName) : AdFullScreenUiResource(adPlaceName)

    data class AdSucceedToShow(val adPlaceName: IAdPlaceName) : AdFullScreenUiResource(adPlaceName)

    data class AdDismissed(
        val adPlaceName: IAdPlaceName,
        val isEarnedReward: Boolean,
        val amount: Int
    ) : AdFullScreenUiResource(adPlaceName)

    data class AdCompleted(val adPlaceName: IAdPlaceName, val isShown: Boolean, val isEarnedReward: Boolean = false) : AdFullScreenUiResource(adPlaceName)

}