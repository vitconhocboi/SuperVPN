package com.core.config.data

sealed class FetchRemoteConfigState {
    object Loading : FetchRemoteConfigState()

    data class Complete(
        val isSuccess: Boolean,
    ) : FetchRemoteConfigState()
}