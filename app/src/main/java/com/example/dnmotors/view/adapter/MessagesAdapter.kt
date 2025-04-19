package com.example.dnmotors.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.databinding.UserListBinding
import com.example.domain.model.Message

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(private val binding: UserListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) = with(binding) {
            tvMessage.text = message.message
            tvUser.text = message.name
        }

        companion object {
            fun create(parent: ViewGroup): ItemHolder {
                val binding = UserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ItemHolder(binding)
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }
}