package com.core.ads.domain


sealed class ConsentFormUiResource {

    object Loading : ConsentFormUiResource()
    object Showing : ConsentFormUiResource()
    object Complete : ConsentFormUiResource()

}