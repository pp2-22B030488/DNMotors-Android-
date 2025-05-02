package com.example.dnmotors.view.fragments.messagesFragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.example.domain.util.FileUtils
import com.example.dnmotors.view.adapter.MessagesAdapter
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.AuthUser
import com.example.domain.model.Message
import com.example.domain.repository.MediaRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.UUID

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var auth: AuthUser
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRef: CollectionReference
    private lateinit var mediaRepository: MediaRepository
    private var snapshotListener: ListenerRegistration? = null
    private val chatViewModel: ChatViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModel()

    private lateinit var carId: String
    private lateinit var dealerId: String

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startAudioRecording()
            } else {
                showToast("Audio permission denied")
            }
        }

    override fun onStart() {
        super.onStart()
        loadMessages()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        setupDependencies()
        setupRecyclerView()
        setupClickListeners()
        return binding.root
    }

    private fun setupDependencies() {
        mediaRepository = MediaRepository(requireContext())

        val args = MessagesFragmentArgs.fromBundle(requireArguments())
        carId = args.carId
        dealerId = args.dealerId

        lifecycleScope.launch {
            auth = authViewModel.returnAuth()
        }
        firestore = FirebaseFirestore.getInstance()

        val currentUserId = auth.uid ?: run {
            showToast("User not logged in")
            return
        }

        val chatId = "${dealerId}_${currentUserId}"
        messagesRef = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
    }

    private fun setupRecyclerView() {
        adapter = MessagesAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@MessagesFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            sendTextMessage()
        }
        setupAudioRecordingButton()
    }

    private fun sendTextMessage() {
        val currentUserId = auth.uid ?: return
        val chatId = "${dealerId}_${currentUserId}"
        val messageText = binding.messageInput.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val encodedText = Base64.encodeToString(messageText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)

            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                dealerId = dealerId,
                userId = currentUserId,
                name = auth.displayName ?: "Unknown User",
                text = messageText,
                mediaData = encodedText,
                messageType = "text",
                timestamp = System.currentTimeMillis(),
                carId = carId,
                notificationSent = false
            )

            chatViewModel.sendMessage(message, chatId)
            binding.messageInput.text.clear()
        }

    }

    private fun setupAudioRecordingButton() {
        binding.audioRecordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    checkAudioPermissionAndStartRecording()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopAudioRecording()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkAudioPermissionAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startAudioRecording()
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startAudioRecording() {
        mediaRepository.startRecording(
            onStart = { showToast("Recording started") },
            onFailure = { error -> showToast(error) }
        )
    }

    private fun stopAudioRecording() {
        if (!mediaRepository.isRecording()) {
            mediaRepository.cleanupRecorder()
            return
        }

        mediaRepository.stopRecording(
            onSuccess = { file ->
                handleRecordedAudio(file)
            },
            onFailure = {
                showToast("Recording failed")
                mediaRepository.cleanupRecorder()
            }
        )
    }

    private fun handleRecordedAudio(file: File) {
        val currentUserId = auth.uid ?: return
        val chatId = "${dealerId}_${currentUserId}"
        val base64 = FileUtils.fileToBase64(file)

        if (base64.isNotEmpty()) {
            val message = Message(
                id = System.currentTimeMillis().toString(),
                senderId = currentUserId,
                userId = currentUserId,
                dealerId = dealerId,
                name = auth.displayName ?: "Unknown User",
                mediaData = base64,
                messageType = "audio",
                timestamp = System.currentTimeMillis(),
                carId = carId,
                notificationSent = false
            )

            chatViewModel.sendMessage(message, chatId)
        }
    }


    private fun loadMessages() {
        val currentUserId = auth.uid ?: return
        val chatId = "${dealerId}_${currentUserId}"

        chatViewModel.loadMessages(chatId)

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            if (messages.isNullOrEmpty()) return@observe

            adapter.submitList(messages) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
        if (mediaRepository.isRecording()) {
            mediaRepository.stopRecording(
                onSuccess = { file -> file.delete() },
                onFailure = { mediaRepository.cleanupRecorder() }
            )
        }
    }
}