package com.akmobile.supervpn.language

import com.akmobile.supervpn.R


enum class LangType(val langNameStringId: Int, val flagDrawableId: Int, val langCode: String) {

    DEFAULT(R.string.lang_default, R.drawable.ic_flag_default, ""),
    EN(R.string.lang_en, R.drawable.ic_flag_england, "en"),
    VI(R.string.lang_vi, R.drawable.ic_flag_vietnam, "vi"),
    ARAB(R.string.lang_ar, R.drawable.ic_flag_arab, "ar"),
    HI(R.string.lang_hi, R.drawable.ic_flag_india, "hi"),
    IN(R.string.lang_in, R.drawable.ic_flag_indonesia, "in"),
    MS(R.string.lang_ms, R.drawable.ic_flag_malay, "ms"),
    RU(R.string.lang_ru, R.drawable.ic_flag_russian, "ru"),
    PT(R.string.lang_pt, R.drawable.ic_flag_potuguese, "pt"),
    FR(R.string.lang_fr, R.drawable.ic_flag_french, "fr"),
    DE(R.string.lang_de, R.drawable.ic_flag_germany, "de"),
    ES(R.string.lang_es, R.drawable.ic_flag_spain, "es"),
    JA(R.string.lang_ja, R.drawable.ic_flag_japan, "ja"),
    KO(R.string.lang_ko, R.drawable.ic_flag_korea, "ko")
}