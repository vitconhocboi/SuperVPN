package com.core.ads.domain

import com.core.config.domain.data.IAdPlaceName

sealed class AdOpenAdUiResource(val rootAdPlaceName: IAdPlaceName) {

    data class AdLoaded(val adPlaceName: IAdPlaceName) : AdOpenAdUiResource(adPlaceName)

    data class AdShowing(val adPlaceName: IAdPlaceName) : AdOpenAdUiResource(adPlaceName)

    data class AdDismissed(val adPlaceName: IAdPlaceName) : AdOpenAdUiResource(adPlaceName)

    data class AdNotValidOrLoadFailed(val adPlaceName: IAdPlaceName) : AdOpenAdUiResource(adPlaceName)

}