package com.common.baseui

data class ResultData<out T> private constructor(val status: State, val data: T?, val errorException: Throwable? = null) {
    companion object {
        fun <T> success(data: T): ResultData<T> =
            ResultData(
                status = State.SUCCESS,
                data = data
            )

        fun <T> error(errorException: Throwable?): ResultData<T> =
            ResultData(
                status = State.ERROR,
                data = null,
                errorException = errorException
            )

        fun <T> loading(): ResultData<T> =
            ResultData(
                status = State.LOADING,
                data = null
            )

        fun <T> standby(): ResultData<T> =
            ResultData(
                status = State.STANDBY,
                data = null
            )
    }

    enum class State {
        LOADING,
        STANDBY,
        SUCCESS,
        ERROR
    }
}

abstract class Mapper<MODEL, UI, PARAMS> {
    abstract fun mapToUISuccess(params:PARAMS, data: ResultData<MODEL>): ResultData<UI>
    fun mapToUI(params:PARAMS, data: ResultData<MODEL>): ResultData<UI> {
        return when(data.status) {
            ResultData.State.STANDBY -> {
                ResultData.standby()
            }

            ResultData.State.LOADING -> {
                ResultData.loading()
            }

            ResultData.State.SUCCESS -> {
                mapToUISuccess(params, data)
            }

            ResultData.State.ERROR -> {
                ResultData.error(data.errorException)
            }
        }
    }


}
