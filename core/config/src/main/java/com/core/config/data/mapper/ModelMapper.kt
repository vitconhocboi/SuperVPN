package com.core.config.data.mapper

internal interface ModelMapper<M, D> {
    fun toData(model: M): D
}