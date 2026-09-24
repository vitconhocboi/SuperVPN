package com.tici.vpn.proxy.master.settings.adsblock

import com.tici.vpn.proxy.master.db.AdRuleDB

fun AdRuleDB.mapToUI(): AdRuleUI {
    return AdRuleUI(id, domain, displayName, enabled, isDefault)
}
