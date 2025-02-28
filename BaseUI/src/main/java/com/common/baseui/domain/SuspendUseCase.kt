package com.common.baseui.domain

interface SuspendUseCase<in Params, out T> {
    suspend fun execute(params: Params) : T
}