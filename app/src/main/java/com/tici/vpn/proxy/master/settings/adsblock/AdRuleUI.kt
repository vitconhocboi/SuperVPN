package com.tici.vpn.proxy.master.settings.adsblock

data class AdRuleUI(
    val id: Long,
    val domain: String,
    val displayName: String,
    var enabled: Boolean,
    val isDefault: Boolean
)
