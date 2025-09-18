package com.core.utilities.result

import com.core.utilities.result.ObjectUiState.Success
import com.core.utilities.result.PageUiState.Content

sealed class ObjectUiState<out T> {
    /**
     * Trạng thái tải lần đầu tiên, khi chưa có dữ liệu gì.
     * UI nên hiển thị một ProgressBar chiếm toàn bộ không gian.
     */
    data object Loading : ObjectUiState<Nothing>()
    data object StandBy : ObjectUiState<Nothing>()

    /**
     * Trạng thái khi việc tải lần đầu thất bại.
     * UI nên hiển thị giao diện lỗi toàn màn hình với nút "Thử lại".
     */
    data class Error(val exception: Throwable) : ObjectUiState<Nothing>()

    /**
     * Trạng thái đã có dữ liệu thành công.
     * Trạng thái này cũng quản lý các tác vụ con như "làm mới".
     */
    data class Success<T>(
        // Dữ liệu của đối tượng đã tải thành công.
        val data: T,

        // Cờ cho biết có đang làm mới (refresh) dữ liệu hay không.
        // UI có thể dựa vào đây để hiển thị một ProgressBar nhỏ ở đâu đó.
        val isRefreshing: Boolean = false,

        // Chứa lỗi của lần làm mới gần nhất (nếu có).
        // UI có thể dùng để hiển thị một Toast hoặc Snackbar.
        val refreshError: Throwable? = null
    ) : ObjectUiState<T>()
}

inline fun <T> ObjectUiState<T>.processResult(
    onSuccess: (Success<T>) -> Unit,
    onLoading: () -> Unit = {},
    onStandby: () -> Unit = {},
    onError: (Throwable?) -> Unit = {}
) {
    when (this) {
        is ObjectUiState.StandBy -> {
            onStandby.invoke()
        }

        is ObjectUiState.Loading -> {
            onLoading.invoke()
        }

        is ObjectUiState.Error -> {
            onError.invoke(exception)
        }

        is Success -> {
            onSuccess.invoke(this)
        }
    }
}

fun <T>Result<T>.mapToObjectUiState(): ObjectUiState<T> {
    return fold(
        onSuccess = { ObjectUiState.Success(it) },
        onFailure = { ObjectUiState.Error(it) }
    )
}

fun <T, V>Result<T>.transformToObjectUiState(
    transform: (T) -> V
): ObjectUiState<V> {
    return fold(
        onSuccess = { ObjectUiState.Success(transform(it)) },
        onFailure = { ObjectUiState.Error(it) }
    )
}

