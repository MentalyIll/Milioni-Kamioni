package com.example.stayfree.ui.inapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.stayfree.data.local.entity.InAppBlockEntity
import com.example.stayfree.databinding.ItemInAppBlockBinding
import com.example.stayfree.util.AppInfoUtils

class InAppBlockAdapter(
    private val onToggle: (InAppBlockEntity) -> Unit
) : ListAdapter<InAppBlockEntity, InAppBlockAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInAppBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInAppBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: InAppBlockEntity) {
            binding.tvFeatureName.text = entity.featureName
            binding.tvAppName.text = AppInfoUtils.getAppName(binding.root.context, entity.targetApp)
            val icon = AppInfoUtils.getAppIcon(binding.root.context, entity.targetApp)
            if (icon != null) binding.ivAppIcon.setImageDrawable(icon)
            binding.switchActive.isChecked = entity.isActive
            binding.switchActive.setOnCheckedChangeListener { _, _ -> onToggle(entity) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<InAppBlockEntity>() {
            override fun areItemsTheSame(a: InAppBlockEntity, b: InAppBlockEntity) = a.id == b.id
            override fun areContentsTheSame(a: InAppBlockEntity, b: InAppBlockEntity) = a == b
        }
    }
}
