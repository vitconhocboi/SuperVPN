package com.hoangsinh.supervpn.language

class LangDataModel(val langName:String, val lang: LangType, var isSelected: Boolean = false) {
    val langCode = lang.langCode
    val flagDrawableId = lang.flagDrawableId
    var otherName: String = ""
}