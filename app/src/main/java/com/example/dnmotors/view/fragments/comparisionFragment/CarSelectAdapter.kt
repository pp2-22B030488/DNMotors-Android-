package com.example.dnmotors.view.fragments.comparisionFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.databinding.ItemCarBinding
import com.example.dnmotors.view.fragments.carFragment.Car

class CarSelectAdapter(
    private val onClick: (Car) -> Unit
) : ListAdapter<Car, CarSelectAdapter.CarViewHolder>(CarDiffCallback()) {

    inner class CarViewHolder(private val binding: ItemCarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(car: Car) {
            binding.textBrandModel.text = car.model
            binding.textYear.text = "${car.year}, ${car.price}$"
            binding.root.setOnClickListener {
                onClick(car)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean = oldItem == newItem
    }
}
