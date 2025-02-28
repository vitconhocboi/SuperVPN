package com.common.baseui.extension

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class OnScrollLoadMore(val layoutManager: RecyclerView.LayoutManager, var onLoadMore: () -> Unit, var visibleThreshold: Int = 3) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (layoutManager is GridLayoutManager) {
            val visibleItemCount = layoutManager.getChildCount();
            val totalItemCount = layoutManager.getItemCount();
            val pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

            if ((visibleItemCount + pastVisiblesItems + visibleThreshold) >= totalItemCount) {
                onLoadMore.invoke()
            }
        } else if(layoutManager is LinearLayoutManager){
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
            if (totalItemCount <= lastVisibleItem + visibleThreshold) {
                onLoadMore.invoke()
            }
        } else if(layoutManager is StaggeredGridLayoutManager) {
            val visibleItemCount = layoutManager.getChildCount();
            val totalItemCount = layoutManager.getItemCount();
            val pastVisiblesItems = layoutManager.findFirstVisibleItemPositions(null)

            if ((visibleItemCount + pastVisiblesItems[0] + visibleThreshold) >= totalItemCount) {
                onLoadMore.invoke()
            }
        }


    }
}