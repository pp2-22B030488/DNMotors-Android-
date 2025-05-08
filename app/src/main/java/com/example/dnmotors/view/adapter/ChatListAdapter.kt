package com.example.dnmotors.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ChatItemBinding
import com.example.domain.model.ChatItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ChatListAdapter(
    private val items: List<ChatItem>,
    private val onClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem) {
//            binding.chatTitle.text = "Chat for ${item.name}"
//            binding.root.setOnClickListener { onClick(item) }
//            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightGray))
//            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightGray))

            binding.dealerName.text = item.dealerName
            binding.messageTime.text = formatTimestamp(item.timestamp)
            binding.carName.text = item.brand + " " + item.model + " " + item.year
            binding.lastMessage.text = item.lastMessage

            val context = binding.carImage.context
            val imageUrl = item.imageUrl.firstOrNull()

            if (imageUrl != null) {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background) // или свой плейсхолдер
                        .centerCrop()
                    .into(binding.carImage)
            } else {
                binding.carImage.setImageResource(R.drawable.ic_launcher_background)
            }

            // Цвет фона (если нужно менять динамически, например, по состоянию)
//            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.lightGray))

            // Обработка клика
            binding.root.setOnClickListener { onClick(item) }

        }
    }
    fun formatTimestamp(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val now = Calendar.getInstance()

        val isToday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return when {
            isToday -> "Сегодня, ${timeFormat.format(Date(timestamp))}"
            isYesterday -> "Вчера, ${timeFormat.format(Date(timestamp))}"
            else -> SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
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
