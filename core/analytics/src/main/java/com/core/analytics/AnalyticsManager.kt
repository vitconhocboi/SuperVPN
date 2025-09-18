package com.core.analytics

interface AnalyticsManager {

    fun trackScreen(screenName: String)

    fun logEvent(eventName: String, params: Map<String, String> = mapOf())
    fun logEvent(eventName: String)
}