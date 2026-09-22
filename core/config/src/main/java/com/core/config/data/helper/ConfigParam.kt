package com.core.config.data.helper

import com.core.config.data.model.AppConfigModel
import com.core.config.data.model.IapConfigModel

internal sealed class ConfigParam<T : Any> {
    abstract val key: String

    internal object AppConfig: ConfigParam<AppConfigModel>() {

        override val key = "application_config"

    }

    internal object IapConfig: ConfigParam<IapConfigModel>() {

        override val key = "iap_config"

    }
}
