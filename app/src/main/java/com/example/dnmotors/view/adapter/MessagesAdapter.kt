package com.example.dnmotors.view.adapter

import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.databinding.UserListBinding
import com.example.dnmotors.utils.MediaUtils
import com.example.dnmotors.utils.MediaUtils.decodeFromBase64
import com.example.domain.model.Message
import java.io.File

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(private val binding: UserListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) = with(binding) {
            tvUser.text = message.name

            when (message.mediaType) {
                "audio", "video" -> {
                    tvMessage.text = "[${message.mediaType} message]"
                    btnPlay.apply {
                        text = "Play"
                        visibility = View.VISIBLE
                        setOnClickListener {
                            val file = message.base64?.let {
                                MediaUtils.decodeBase64ToFile(it, message.mediaType!!, itemView.context)
                            }

                            if (file != null) {
                                MediaUtils.playFile(file, message.mediaType!!, itemView.context)
                            } else {
                                Toast.makeText(itemView.context, "Failed to load media", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }


                }
                else -> {
                    btnPlay.visibility = View.GONE
                    tvMessage.text = if (!message.message.isNullOrEmpty())
                        decodeFromBase64(message.message!!) else ""
                }
            }

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