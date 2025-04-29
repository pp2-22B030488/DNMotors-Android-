package com.example.dnmotors.view.fragments.messagesFragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.example.domain.util.FileUtils
import com.example.dnmotors.view.adapter.MessagesAdapter
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.example.domain.repository.MediaRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRef: CollectionReference
    private lateinit var mediaRepository: MediaRepository
    private var snapshotListener: ListenerRegistration? = null
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var carId: String
    private lateinit var dealerId: String

    override fun onStart() {
        super.onStart()
        loadMessagesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        mediaRepository = MediaRepository(requireContext())
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val args = MessagesFragmentArgs.fromBundle(requireArguments())
        carId = args.carId
        dealerId = args.dealerId

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
            return binding.root
        }

        firestore = FirebaseFirestore.getInstance()

        val currentUserId = auth.currentUser?.uid
        val chatId = "${dealerId}_${currentUserId}"

        messagesRef = FirebaseFirestore.getInstance().collection("chats")
            .document(chatId)
            .collection("messages")

        adapter = MessagesAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            if (currentUserId != null) {
                chatViewModel.sendMessage(
                    chatId = chatId,
                    carId = carId,
                    messageText = binding.messageInput.text.toString().trim(),
                    senderName = auth.currentUser?.displayName ?: "Unknown User",
                    senderId = currentUserId,
                    userId = dealerId,
                    notificationSent = false)
            }
        }
        setupAudioRecordButton()
        return binding.root
    }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupAudioRecordButton()
            } else {
                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun setupAudioRecordButton() {

        val currentUserId = auth.currentUser?.uid
        val chatId = "${dealerId}_${currentUserId}"

        binding.audioRecordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mediaRepository.isRecording()) return@setOnTouchListener true

                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startRecordingSafely()
                    } else {
                        mediaRepository.requestAudioPermission(
                            requireActivity(),
                            requestAudioPermissionLauncher,
                            onPermissionGranted = { startRecordingSafely() },
                            onPermissionDenied = {
                                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (mediaRepository.isRecording()) {
                        mediaRepository.stopRecording(
                            onSuccess = { file ->
                                val base64 = FileUtils.fileToBase64(file)
                                if (base64.isNotEmpty()) {
                                    if (currentUserId != null) {
                                        chatViewModel.sendMediaMessage(
                                            chatId = chatId,
                                            carId = carId,
                                            senderName = auth.currentUser?.displayName ?: "Unknown User",
                                            senderId = currentUserId,
                                            base64Media = base64,
                                            type = "audio")
                                    }
                                }
                            },
                            onFailure = {
                                mediaRepository.cleanupRecorder()
                            }
                        )
                    } else {
                        mediaRepository.cleanupRecorder()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startRecordingSafely() {
        mediaRepository.startRecording(
            onStart = {
                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
            },
            onFailure = { err ->
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadMessagesFragment() {
        val currentUserId = auth.currentUser?.uid
        val chatId = "${dealerId}_${currentUserId}"

        chatViewModel.loadMessages(chatId)

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            if (messages.isNullOrEmpty()) return@observe

            adapter.submitList(messages) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }
}