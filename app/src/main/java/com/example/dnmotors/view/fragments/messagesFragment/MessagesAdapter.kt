package com.example.dnmotors.app.presentation.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ItemMessageBinding
import com.example.domain.model.Message

class MessagesAdapter(
    private val context: Context,
    private val currentUserId: String
) : ListAdapter<Message, MessagesAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.messageText.text = message.text
            binding.senderName.text = message.senderName

            val isUserMessage = message.senderId == currentUserId
            val params = binding.messageCard.layoutParams as FrameLayout.LayoutParams
            params.gravity = if (isUserMessage) Gravity.END else Gravity.START
            binding.messageCard.layoutParams = params

            val bgColor = if (isUserMessage) {
                ContextCompat.getColor(context, R.color.user_message)
            } else {
                ContextCompat.getColor(context, R.color.dealer_message)
            }
            binding.messageCard.setCardBackgroundColor(bgColor)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) =
            oldItem.timestamp == newItem.timestamp

        override fun areContentsTheSame(oldItem: Message, newItem: Message) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}