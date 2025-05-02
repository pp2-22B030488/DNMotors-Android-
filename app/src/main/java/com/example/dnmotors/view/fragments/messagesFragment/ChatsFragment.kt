package com.example.dnmotors.view.fragments.messagesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentChatsBinding
import com.example.dnmotors.view.adapter.ChatListAdapter
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.AuthUser
import com.example.domain.model.ChatItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatsFragment : Fragment() {
    private val authViewModel: AuthViewModel by viewModel()
    private lateinit var binding: FragmentChatsBinding
    private lateinit var auth: AuthUser
    private lateinit var firestore: FirebaseFirestore
    private val chatViewModel: ChatViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            auth = authViewModel.returnAuth()
        }
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.uid ?: return binding.root
        chatViewModel.loadChatList(false)

        chatViewModel.chatItems.observe(viewLifecycleOwner, Observer { items ->
            setupRecyclerView(items)
        })
        return binding.root
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
