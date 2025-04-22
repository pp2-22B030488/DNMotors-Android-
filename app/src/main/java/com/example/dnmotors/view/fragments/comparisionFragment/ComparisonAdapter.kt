package com.example.dnmotors.view.fragments.comparisionFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.databinding.ItemComparisonBinding
import com.example.dnmotors.view.fragments.comparisionFragment.CarComparisonFragment.ComparisonItem

class ComparisonAdapter : ListAdapter<ComparisonItem, ComparisonAdapter.ComparisonViewHolder>(
    ComparisonDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComparisonViewHolder {
        val binding = ItemComparisonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComparisonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComparisonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ComparisonViewHolder(
        private val binding: ItemComparisonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ComparisonItem) {
            binding.tvParameterName.text = item.parameterName
            binding.tvCar1Value.text = item.car1Value
            binding.tvCar2Value.text = item.car2Value
        }
    }

    private class ComparisonDiffCallback : DiffUtil.ItemCallback<ComparisonItem>() {
        override fun areItemsTheSame(oldItem: ComparisonItem, newItem: ComparisonItem): Boolean {
            return oldItem.parameterName == newItem.parameterName
        }

        override fun areContentsTheSame(oldItem: ComparisonItem, newItem: ComparisonItem): Boolean {
            return oldItem == newItem
        }
    }
} 