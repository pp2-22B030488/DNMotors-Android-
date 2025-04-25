package com.example.dnmotors.view.fragments.messagesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentChatsBinding
import com.example.dnmotors.view.adapter.ChatListAdapter
import com.example.domain.model.ChatItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val _chatItems = MutableLiveData<List<ChatItem>>()
    private val chatItems: LiveData<List<ChatItem>> get() = _chatItems

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return binding.root

        loadChatListForUser()
        // Observe the chat items to update the RecyclerView
        chatItems.observe(viewLifecycleOwner, Observer { items ->
            setupRecyclerView(items)
        })
        return binding.root
    }
    private fun loadChatListForUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("chats")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val chatItems = result.documents.mapNotNull { doc ->
                    val carId = doc.getString("carId")
                    val dealerId = doc.getString("dealerId")
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                    if (carId != null && dealerId != null) {
                        ChatItem(
                            carId = carId,
                            userId = userId,
                            dealerId = dealerId,
                            timestamp = timestamp,
                        )
                    } else null
                }

                _chatItems.postValue(chatItems)
            }
            .addOnFailureListener { error ->
                println("Error loading user chat list: ${error.message}")
                _chatItems.postValue(emptyList())
            }
    }

    private fun setupRecyclerView(items: List<ChatItem>) {
        val adapter = ChatListAdapter(items) { clickedItem ->
            val action = ChatsFragmentDirections
                .actionChatsFragmentToMessagesFragment(
                    carId = clickedItem.carId,
                    dealerId = clickedItem.dealerId
                )
            findNavController().navigate(action)
        }
        binding.chatsRecyclerView.adapter = adapter
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

}
