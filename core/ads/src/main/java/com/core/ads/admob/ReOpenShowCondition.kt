package com.core.ads.admob

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReOpenShowCondition @Inject constructor(){

    var isCanShow : ()-> Boolean = { true }


}