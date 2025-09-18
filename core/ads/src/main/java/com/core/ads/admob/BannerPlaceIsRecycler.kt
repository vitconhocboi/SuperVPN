package com.core.ads.admob

import com.core.config.domain.data.IAdPlaceName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerPlaceIsRecycler @Inject constructor() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _bannerPlaceIsRecycler = MutableSharedFlow<IAdPlaceName>()
    val bannerPlaceIsRecycler: SharedFlow<IAdPlaceName> = _bannerPlaceIsRecycler

    fun recycleBannerPlace(adPlaceName: IAdPlaceName) {
        applicationScope.launch {
            _bannerPlaceIsRecycler.emit(adPlaceName)
        }
    }
}