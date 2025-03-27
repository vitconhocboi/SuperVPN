package com.akmobile.supervpn.settings.appproxy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akmobile.supervpn.databinding.AppProxyItemBinding

class AppProxyAdapter: ListAdapter<AppInfo, AppProxyAdapter.ViewHolder>(diffUtil) {

    class ViewHolder(val binding: AppProxyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AppProxyItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            appName.text = item.appName
            appIcon.setImageDrawable(item.image)
        }
    }
}

val diffUtil = object : DiffUtil.ItemCallback<AppInfo>() {
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        TODO("Not yet implemented")
    }

}