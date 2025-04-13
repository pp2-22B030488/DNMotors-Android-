package com.example.dnmotors.view.fragments.messagesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.view.fragments.messagesFragment.MessagesAdapter
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("messages")
        adapter = MessagesAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
        return binding.root
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val message = Message(
                id = auth.currentUser?.uid,
                name = auth.currentUser?.displayName ?: "Unknown",
                message = messageText,
                timestamp = System.currentTimeMillis()
            )
            database.push().setValue(message)
            binding.messageInput.text.clear()
        }
    }

    private fun listenForMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (s in snapshot.children) {
                    val message = s.getValue(Message::class.java)
                    if (message != null) messages.add(message)
                }
                adapter.submitList(messages.toList())
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
