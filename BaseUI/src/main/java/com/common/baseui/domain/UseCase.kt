package com.common.baseui.domain

interface UseCase<in Params, out T> {
    fun execute(params: Params): T
}