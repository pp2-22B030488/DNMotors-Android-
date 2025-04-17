package com.example.dnmotors.app.presentation.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.app.presentation.adapters.MessagesAdapter
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.example.dnmotors.viewmodel.MessagesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private val viewModel: MessagesViewModel by viewModels()
    private lateinit var adapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dealerId = arguments?.getString("dealerId") ?: ""
        viewModel.initChat(dealerId)

        setupRecyclerView()
        setupObservers()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        adapter = MessagesAdapter(
            requireContext(),
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessagesFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collectLatest { messages ->
                    adapter.submitList(messages)
                }
            }
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.messageInput.text.clear()
            }
        }
    }
}