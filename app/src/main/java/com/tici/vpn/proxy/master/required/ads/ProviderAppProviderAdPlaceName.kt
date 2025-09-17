package com.tici.vpn.proxy.master.required.ads

import com.core.config.domain.data.IAdPlaceName
import com.core.config.domain.data.IAppProviderAdPlaceName
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderAppProviderAdPlaceName @Inject constructor(): IAppProviderAdPlaceName {
    override fun findAdPlaceName(key: String): IAdPlaceName? {
        return AppAdPlaceName.fromKey(key)
    }
}