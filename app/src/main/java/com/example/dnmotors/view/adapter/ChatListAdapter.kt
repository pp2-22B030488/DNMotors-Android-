package com.example.dnmotors.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ChatItemBinding
import com.example.domain.model.ChatItem


class ChatListAdapter(
    private val items: List<ChatItem>,
    private val onClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
            binding.chatTitle.text = "Chat for ${item.name}"
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightGray))
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightGray))

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
