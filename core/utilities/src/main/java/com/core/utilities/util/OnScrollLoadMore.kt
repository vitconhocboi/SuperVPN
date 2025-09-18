package com.core.utilities.util

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class OnScrollLoadMore(
    private val onLoadMore: () -> Unit,
    private val visibleThreshold: Int = 5 // Tăng buffer mặc định lên một chút
) : RecyclerView.OnScrollListener() {

    // Biến cờ để tránh gọi onLoadMore liên tục
    private var isLoading = false
    private var lastTotalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // Bỏ qua nếu người dùng cuộn lên hoặc không có layout manager
        val layoutManager = recyclerView.layoutManager ?: return
        if (dy <= 0) return

        val totalItemCount = layoutManager.itemCount

        // Nếu tổng số item giảm (ví dụ: khi refresh), reset lại trạng thái
        if (totalItemCount < lastTotalItemCount) {
            lastTotalItemCount = totalItemCount
            isLoading = false
            return
        }

        // Nếu đang loading và số item không tăng, không làm gì cả
        if (isLoading && totalItemCount > lastTotalItemCount) {
            isLoading = false
            lastTotalItemCount = totalItemCount
        }

        if (!isLoading) {
            val lastVisibleItemPosition = when (layoutManager) {
                is StaggeredGridLayoutManager -> {
                    val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                    // Lấy vị trí lớn nhất trong các cột
                    getLastVisibleItem(lastVisibleItemPositions)
                }
                is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
                is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
                else -> 0
            }

            // Điều kiện để gọi load more
            if (lastVisibleItemPosition + visibleThreshold >= totalItemCount && totalItemCount > 0) {
                onLoadMore()
                isLoading = true
            }
        }
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0 || lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // Hàm để reset lại trạng thái từ bên ngoài nếu cần
    fun setLoaded() {
        isLoading = false
    }
}