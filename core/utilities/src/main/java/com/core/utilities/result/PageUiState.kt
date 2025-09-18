package com.core.utilities.result

import com.core.utilities.result.PageUiState.Content

sealed class PageUiState<out T> {

    data object StandBy : PageUiState<Nothing>()

    /**
     * Trạng thái tải lần đầu (First Load)
     * UI sẽ hiển thị một ProgressBar toàn màn hình.
     */
    data object FirstLoading : PageUiState<Nothing>()

    /**
     * Trạng thái tải lần đầu thất bại (First Load Failed)
     * UI sẽ hiển thị một giao diện lỗi toàn màn hình với nút "Thử lại".
     */
    data class FirstLoadError(val exception: Throwable) : PageUiState<Nothing>()

    /**
     * Trạng thái hiển thị nội dung thành công.
     * Đây là trạng thái chính, chứa dữ liệu và cả các trạng thái con cho việc "tải thêm".
     */
    data class Content<T>(
        // Dữ liệu chính đang được hiển thị
        val data: T,

        // Cờ cho biết có đang "tải thêm" hay không (để hiển thị ProgressBar ở cuối danh sách)
        val isLoadingMore: Boolean = false,

        // Chứa lỗi của lần "tải thêm" gần nhất (để hiển thị nút Retry ở cuối danh sách)
        val loadMoreError: Throwable? = null,

        // Cờ cho biết có thể tải thêm được nữa không
        val canLoadMore: Boolean = true
    ) : PageUiState<T>()

    fun getLoading(): PageUiState<T> {
        return when (this) {
            is StandBy -> FirstLoading
            is FirstLoading -> this
            is FirstLoadError -> this
            is Content -> {
                if(data is List<*> && data.isEmpty()) {
                    FirstLoading
                } else {
                    Content(data, true, null, canLoadMore)
                }
            }
        }
    }
}

inline fun <T> PageUiState<T>.processResult(
    onResult: (Content<T>) -> Unit,
    onFirstLoading: () -> Unit = {},
    onStandby: () -> Unit = {},
    onFirstLoadError: (Throwable?) -> Unit = {}
) {
    when (this) {
        is PageUiState.StandBy -> {
            onStandby.invoke()
        }

        is PageUiState.FirstLoading -> {
            onFirstLoading.invoke()
        }

        is PageUiState.FirstLoadError -> {
            onFirstLoadError.invoke(exception)
        }

        is Content -> {
            onResult.invoke(this)
        }
    }
}