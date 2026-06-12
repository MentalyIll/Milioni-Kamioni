package com.example.stayfree.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfree.databinding.ItemAppUsageBinding
import com.example.stayfree.domain.model.AppUsage
import com.example.stayfree.util.AppInfoUtils
import com.example.stayfree.util.TimeUtils

class AppUsageListAdapter(
    private val onClick: (AppUsage) -> Unit
) : ListAdapter<AppUsage, AppUsageListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppUsageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppUsageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appUsage: AppUsage) {
            binding.tvAppName.text = appUsage.appName
            binding.tvUsageTime.text = TimeUtils.formatDuration(appUsage.totalTimeMs)
            binding.tvUnlockCount.text = "${appUsage.unlockCount} unlocks"
            val icon = AppInfoUtils.getAppIcon(binding.root.context, appUsage.packageName)
            if (icon != null) binding.ivAppIcon.setImageDrawable(icon)
            binding.root.setOnClickListener { onClick(appUsage) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppUsage>() {
            override fun areItemsTheSame(a: AppUsage, b: AppUsage) = a.packageName == b.packageName && a.date == b.date
            override fun areContentsTheSame(a: AppUsage, b: AppUsage) = a == b
        }
    }
}
