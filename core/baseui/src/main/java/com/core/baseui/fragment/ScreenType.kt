package com.core.baseui.fragment

interface ScreenType {
    val screenName: String
}

sealed class CoreScreenType(override val screenName: String) : ScreenType {
    object Main : CoreScreenType("Main")
}