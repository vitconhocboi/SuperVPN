package com.core.baseui.supportedlanguage

sealed class SupportedLanguage {
    abstract val languageCode: String
    abstract val displayName: String
    open val rightToLeft: RightToLeft = RightToLeft.No
    var isSelected: Boolean = false

    sealed class RightToLeft {
        abstract val viewType: Int

        object No: RightToLeft() {
            override val viewType = 1
        }

        object Yes: RightToLeft() {
            override val viewType = 2
        }
    }
    object DEFAULT: SupportedLanguage() {
        override val languageCode = ""
        override val displayName = ""
    }

    object ENGLISH: SupportedLanguage() {
        override val languageCode = "en"
        override val displayName = "English"
    }

    object FRENCH: SupportedLanguage() {
        override val languageCode = "fr"
        override val displayName = "Français"
    }

    object GERMAN: SupportedLanguage() {
        override val languageCode = "de"
        override val displayName = "Deutsche"
    }

    object ITALIAN: SupportedLanguage() {
        override val languageCode = "it"
        override val displayName = "Italiano"
    }

    object PORTUGUESE: SupportedLanguage() {
        override val languageCode = "pt"
        override val displayName = "Português"
    }

    object SPANISH: SupportedLanguage() {
        override val languageCode = "es"
        override val displayName = "Español"
    }

    object DUTCH: SupportedLanguage() {
        override val languageCode = "nl"
        override val displayName = "Nederlands"
    }

    object FINNISH: SupportedLanguage() {
        override val languageCode = "fi"
        override val displayName = "Suomen kieli"
    }

    object POLISH: SupportedLanguage() {
        override val languageCode = "pl"
        override val displayName = "Polskie"
    }

    object SWEDISH: SupportedLanguage() {
        override val languageCode = "sv"
        override val displayName = "Svenska"
    }

    object SERBIAN: SupportedLanguage() {
        override val languageCode = "sr"
        override val displayName = "Српски језик"
    }

    object SLOVAK: SupportedLanguage() {
        override val languageCode = "sk"
        override val displayName = "Slovenčina"
    }

    object NORWEGIAN: SupportedLanguage() {
        override val languageCode = "no"
        override val displayName = "Norsk"
    }

    object CZECH: SupportedLanguage() {
        override val languageCode = "cs"
        override val displayName = "Čeština"
    }

    object CATALAN: SupportedLanguage() {
        override val languageCode = "ca"
        override val displayName = "Català"
    }

    object ESTONIAN: SupportedLanguage() {
        override val languageCode = "et"
        override val displayName = "Eesti Keel"
    }

    object ICELANDIC: SupportedLanguage() {
        override val languageCode = "is"
        override val displayName = "Íslenska"
    }

    object LATVIAN: SupportedLanguage() {
        override val languageCode = "lv"
        override val displayName = "Latviešu Valoda"
    }

    object LITHUANIAN: SupportedLanguage() {
        override val languageCode = "lt"
        override val displayName = "Lietuvių Kalba"
    }

    object YIDDISH: SupportedLanguage() {
        override val languageCode = "he"
        override val displayName = "עִבְֿרִיתּ"
        override val rightToLeft = RightToLeft.Yes
    }

    object FILIPINO: SupportedLanguage() {
        override val languageCode = "fil"
        override val displayName = "Wikang Filipino"
    }

    object HUNGARIAN: SupportedLanguage() {
        override val languageCode = "hu"
        override val displayName = "Magyar Nyelv"
    }

    object CROATIAN: SupportedLanguage() {
        override val languageCode = "hr"
        override val displayName = "Hrvatski"
    }

    object DANMARK: SupportedLanguage() {
        override val languageCode = "da"
        override val displayName = "Danmark"
    }

    object GREEK: SupportedLanguage() {
        override val languageCode = "el"
        override val displayName = "ελληνικά"
    }

    object RUMANU: SupportedLanguage() {
        override val languageCode = "ro"
        override val displayName = "Română"
    }

    object BULGARIAN: SupportedLanguage() {
        override val languageCode = "bg"
        override val displayName = "България"
    }

    object MALAY: SupportedLanguage() {
        override val languageCode = "ms"
        override val displayName = "Bahasa melayu"
    }

    object BENGALI: SupportedLanguage() {
        override val languageCode = "bn"
        override val displayName = "বাংলা"
    }

    object ARABIC: SupportedLanguage() {
        override val languageCode = "ar"
        override val displayName = "عربى"
        override val rightToLeft = RightToLeft.Yes
    }

    object RUSSIAN: SupportedLanguage() {
        override val languageCode = "ru"
        override val displayName = "Pусский"
        
    }

    object HINDI: SupportedLanguage() {
        override val languageCode = "hi"
        override val displayName = "हिंदी"
    }

    object CHINA_SIMPLIFIED: SupportedLanguage() {
        override val languageCode = "zh"
        override val displayName = "简体中文"
    }

    object CHINA_TRADITIONAL: SupportedLanguage() {
        override val languageCode = "zh-TW" // Google cloud translation is zh-TW
        override val displayName = "繁体中文"
    }

    object TURKISH: SupportedLanguage() {
        override val languageCode = "tr"
        override val displayName = "Türk (Turkish)"
    }

    object INDONESIAN: SupportedLanguage() {
        override val languageCode = "id"
        override val displayName = "Bahasa Indonesia"
    }

    object URDU: SupportedLanguage() {
        override val languageCode = "ur"
        override val displayName = "اردو"
        override val rightToLeft = RightToLeft.Yes
    }

    object JAPAN: SupportedLanguage() {
        override val languageCode = "ja"
        override val displayName = "日本語"
    }

    object SWAHILI: SupportedLanguage() {
        override val languageCode = "sw"
        override val displayName = "Kiswahili"
    }

    object MARATHI: SupportedLanguage() {
        override val languageCode = "mr"
        override val displayName = "मराठी"
    }

    object TELUGU: SupportedLanguage() {
        override val languageCode = "te"
        override val displayName = "తెలుగు"
    }

    object TAMIL: SupportedLanguage() {
        override val languageCode = "ta"
        override val displayName = "தமிழ்"
    }

    object PUNJABI: SupportedLanguage() {
        override val languageCode = "pa"
        override val displayName = "ਪੰਜਾਬੀ"
    }

    object KOREAN: SupportedLanguage() {
        override val languageCode = "ko"
        override val displayName = "한국어"
    }

    object THAI: SupportedLanguage() {
        override val languageCode = "th"
        override val displayName = "ไทย"
    }

    object PERSIAN: SupportedLanguage() {
        override val languageCode = "fa"
        override val displayName = "فارسی"
        override val rightToLeft = RightToLeft.Yes
    }

    object BURMESE: SupportedLanguage() {
        override val languageCode = "my"
        override val displayName = "မြန်မာစာ"
    }

    object UKRAINA: SupportedLanguage() {
        override val languageCode = "uk"
        override val displayName = "Український"
    }

    object UZBEK: SupportedLanguage() {
        override val languageCode = "uz"
        override val displayName = "Oʻzbek tili"
    }

    object VIETNAMESE: SupportedLanguage() {
        override val languageCode = "vi"
        override val displayName = "Tiếng Việt"
    }

    object KAZAKH: SupportedLanguage() {
        override val languageCode = "kk"
        override val displayName = "Қазақ тілі"
    }

    object ANHARIC: SupportedLanguage() {
        override val languageCode = "am"
        override val displayName = "አማርኛ"
    }

    object GUJARATI: SupportedLanguage() {
        override val languageCode = "gu"
        override val displayName = "ગુજરાતી"
    }

    object KHMER: SupportedLanguage() {
        override val languageCode = "km"
        override val displayName = "ខេមរភាសា"
    }

    object KANNADA: SupportedLanguage() {
        override val languageCode = "kn"
        override val displayName = "ಕನ್ನಡ"
    }

    object MALAYALAM: SupportedLanguage() {
        override val languageCode = "ml"
        override val displayName = "മലയാളം"
    }
}