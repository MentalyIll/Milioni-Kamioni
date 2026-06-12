package com.example.stayfree.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfree.databinding.ItemTopAppBinding
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.AppInfoUtils
import com.example.stayfree.util.TimeUtils

class TopAppsAdapter : ListAdapter<AppUsage, TopAppsAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTopAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appUsage: AppUsage) {
            binding.tvAppName.text = appUsage.appName
            binding.tvUsageTime.text = TimeUtils.formatDuration(appUsage.totalTimeMs)
            val icon = AppInfoUtils.getAppIcon(binding.root.context, appUsage.packageName)
            if (icon != null) binding.ivAppIcon.setImageDrawable(icon)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppUsage>() {
            override fun areItemsTheSame(a: AppUsage, b: AppUsage) = a.packageName == b.packageName && a.date == b.date
            override fun areContentsTheSame(a: AppUsage, b: AppUsage) = a == b
        }
    }
}
