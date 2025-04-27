package com.example.dnmotors.view.fragments.messagesFragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.example.domain.util.FileUtils
import com.example.dnmotors.view.adapter.MessagesAdapter
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.example.domain.repository.MediaRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRef: CollectionReference
    private lateinit var mediaRepository: MediaRepository
    private var snapshotListener: ListenerRegistration? = null

    private lateinit var carId: String
    private lateinit var dealerId: String

    override fun onStart() {
        super.onStart()
        observeMessages()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        mediaRepository = MediaRepository(requireContext())

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
        val chatId = "${dealerId}_$userId"
        messagesRef = firestore.collection("chats")
            .document(chatId)
            .collection("messages")

        adapter = MessagesAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener { sendMessage() }
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
                            onSuccess = { file -> // <- file not path
                                val base64 = FileUtils.fileToBase64(file)
                                if (base64.isNotEmpty()) {
                                    sendBase64Media(base64, "audio")
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

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        val encodedMessage = FileUtils.encodeToBase64(messageText)
        val senderId = auth.currentUser?.uid ?: return
        val senderName = auth.currentUser?.displayName ?: "Unknown User"
        val timestamp = System.currentTimeMillis()
        val chatId = "${dealerId}_$senderId"

        val message = Message(
            id = timestamp.toString(),
            senderId = senderId,
            name = senderName,
            text = messageText,
            mediaData = encodedMessage,
            messageType = "text",
            timestamp = timestamp,
            carId = carId,
            notificationSent = false
        )

        messagesRef.add(message)
            .addOnSuccessListener {
                binding.messageInput.text.clear()
                val chatMetadata = mapOf(
                    "chatId" to chatId,
                    "userId" to senderId,
                    "dealerId" to dealerId,
                    "carId" to carId,
                    "name" to senderName,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("chats").document(chatId)
                    .set(chatMetadata, SetOptions.merge())
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to create chat metadata", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendBase64Media(base64: String, mediaType: String) {
        if (base64.isEmpty()) {
            Toast.makeText(context, "Failed to process media", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            id = System.currentTimeMillis().toString(),
            senderId = auth.currentUser?.uid,
            name = auth.currentUser?.displayName ?: "Unknown User",
            mediaData = base64,
            messageType = mediaType,
            timestamp = System.currentTimeMillis(),
            carId = carId,
            notificationSent = false
        )

        messagesRef.add(message)
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send media", Toast.LENGTH_SHORT).show()
            }
    }

    private fun observeMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = "${dealerId}_$currentUserId"

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    Toast.makeText(context, "Failed to load messages", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val messages = snapshot.documents.mapNotNull { doc ->
                    val message = doc.toObject(Message::class.java)
                    if (message != null && message.senderId != currentUserId && !message.notificationSent) {
                        // Mark as notified
                        doc.reference.update("notificationSent", true)
                    }
                    message
                }

                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }
}