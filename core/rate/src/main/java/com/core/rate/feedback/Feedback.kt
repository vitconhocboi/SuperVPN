package com.core.rate.feedback

class Feedback (
    val content: String,
    val isFeatureQuality: Boolean,
    val isCrash: Boolean,
    val isBug: Boolean,
    val isOthers: Boolean
)