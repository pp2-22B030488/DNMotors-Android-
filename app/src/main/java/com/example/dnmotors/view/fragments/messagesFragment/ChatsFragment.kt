package com.example.dnmotors.view.fragments.messagesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.databinding.ChatItemBinding
import com.example.dnmotors.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return binding.root
        database = FirebaseDatabase.getInstance().getReference("messages").child(userId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carIds = snapshot.children.map { it.key ?: "" }

                val chatItems = carIds.map { carId -> ChatItem(carId = carId) }
                setupRecyclerView(chatItems)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return binding.root
    }

    private fun setupRecyclerView(items: List<ChatItem>) {
        val adapter = ChatListAdapter(items) { clickedCarId ->
            val action = ChatsFragmentDirections
                .actionChatsFragmentToMessagesFragment(carId = clickedCarId)
            findNavController().navigate(action)
        }
        binding.chatsRecyclerView.adapter = adapter
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    data class ChatItem(val carId: String)

    class ChatListAdapter(
        private val items: List<ChatItem>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ChatItem) {
                binding.chatTitle.text = "Chat for ${item.carId}"
                binding.root.setOnClickListener { onClick(item.carId) }
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
}
