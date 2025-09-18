package com.core.config.data.mapper

import com.core.config.data.model.RequestConsentConfigModel
import com.core.config.domain.data.RequestConsentConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RequestConsentConfigModelMapper @Inject constructor(

): ModelMapper<RequestConsentConfigModel, RequestConsentConfig> {

    override fun toData(model: RequestConsentConfigModel): RequestConsentConfig {
        return RequestConsentConfig(
            isEnable = model.isEnable ?: false,
            debugIsEEA = model.debugIsEEA ?: false,
            debugListTestDeviceHashedId = model.debugListTestDeviceHashedId ?: listOf()
        )
    }

}