//package com.example.dnmotors.view.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.domain.model.Chat
//
//// import your Chat model
//
//class ChatsAdapter(
//    private val onChatClick: (Chat) -> Unit // Parameter is the clicked Chat object
//) : ListAdapter<Chat, ChatsAdapter.ChatViewHolder>(DiffCallback()) {
//
//    // ViewHolder remains the same
//    class ChatViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
//        // Consider adding specific IDs if simple_list_item_2 is not sufficient long term
//        private val text1: TextView = view.findViewById(android.R.id.text1)
//        private val text2: TextView = view.findViewById(android.R.id.text2)
//
//        fun bind(chat: Chat, onClick: (Chat) -> Unit) {
//            // Display relevant info. Maybe Car ID isn't the most user-friendly.
//            // Consider fetching Car details (like name/model) if possible.
//            text1.text = "Chat about Car: ${chat.carId}" // Or a more descriptive title
//            text2.text = chat.lastMessage // Show the last message
//            view.setOnClickListener { onClick(chat) }
//        }
//    }
//
//    // DiffCallback remains the same
//    class DiffCallback : DiffUtil.ItemCallback<Chat>() {
//        override fun areItemsTheSame(old: Chat, new: Chat) = old.chatId == new.chatId
//        override fun areContentsTheSame(old: Chat, new: Chat) = old == new // Assumes Chat is a data class
//    }
//
//    // onCreateViewHolder remains the same
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        // Using simple_list_item_2 limits customization. Consider a custom layout.
//        val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
//        return ChatViewHolder(view)
//    }
//
//    // onBindViewHolder remains the same
//    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//        holder.bind(getItem(position), onChatClick)
//    }
//}