package com.example.dnmotors.view.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.databinding.UserListBinding
import com.example.domain.util.MediaUtils
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.ItemHolder>(ItemComparator()) {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_SENDER
    }

    private var onMediaClick: ((Message) -> Unit)? = null

    fun setOnMediaClickListener(listener: (Message) -> Unit) {
        this.onMediaClick = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = UserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemHolder(private val binding: UserListBinding) : RecyclerView.ViewHolder(binding.root) {

        private var imageLoadingJob: kotlinx.coroutines.Job? = null

        fun bind(message: Message) = with(binding) {
            imageLoadingJob?.cancel()

            tvUser.text = message.name ?: "Unknown User"

            tvMessage.visibility = View.GONE
            btnPlay.visibility = View.GONE
            ivMediaPreview.visibility = View.GONE

            when (val messageType = message.messageType) {
                "text" -> {
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = message.text ?: "[Empty Message]"
                }

                "audio", "video" -> {
                    tvMessage.visibility = View.VISIBLE
                    btnPlay.visibility = View.VISIBLE
                    tvMessage.text = "[${messageType.replaceFirstChar { it.uppercase() }} Message]"
                    btnPlay.text = "Play ${messageType.replaceFirstChar { it.uppercase() }}"
                    btnPlay.setOnClickListener {
                        if (message.mediaData.isNullOrEmpty()) {
                            Toast.makeText(itemView.context, "Media file path is missing", Toast.LENGTH_SHORT).show()
                            Log.w("MessagesAdapter", "Play clicked but media file path is null or empty for message ID: ${message.id}")
                            return@setOnClickListener
                        }

                        val file = message.mediaData
                        if (file != null) {
                            MediaUtils.playFile(file, messageType, itemView.context)
                        }
                    }


                }

                "image" -> {
                    ivMediaPreview.visibility = View.VISIBLE
                    ivMediaPreview.setImageResource(R.drawable.ic_settings)

                    val currentBase64 = message.mediaData
                    if (currentBase64.isNullOrEmpty()) {
                        Log.w("MessagesAdapter", "Image message has empty Base64 data. ID: ${message.id}")
                        ivMediaPreview.setImageResource(R.drawable.ic_settings)
                        return@with
                    }


                    imageLoadingJob = CoroutineScope(Dispatchers.Main).launch {
                        Log.d("MessagesAdapter", "Starting background decode for image ID: ${message.id}")
                        val bitmap = decodeBase64ToBitmapAsync(currentBase64)

                        if (bitmap != null) {
                            Log.d("MessagesAdapter", "Image decoded successfully for ID: ${message.id}")
                            ivMediaPreview.setImageBitmap(bitmap)
                        } else {
                            Log.e("MessagesAdapter", "Failed to decode Base64 to Bitmap for image ID: ${message.id}")
                            ivMediaPreview.setImageResource(R.drawable.ic_settings)
                        }
                    }

                }
                else -> {
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = "[Unsupported Message Type: ${message.messageType ?: "null"}]"
                    Log.w("MessagesAdapter", "Unsupported message type encountered: ${message.messageType}")
                }
            }

        }

        private suspend fun decodeBase64ToBitmapAsync(base64String: String): Bitmap? {
            return withContext(Dispatchers.IO) {
                try {
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: IllegalArgumentException) {
                    Log.e("MessagesAdapter", "Invalid Base64 string for image decoding", e)
                    null
                } catch (e: OutOfMemoryError) {
                    Log.e("MessagesAdapter", "OutOfMemoryError decoding image", e)
                    null
                } catch (e: Exception) {
                    Log.e("MessagesAdapter", "Error decoding Base64 to Bitmap", e)
                    null
                }
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
}
