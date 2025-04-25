package com.example.dnmotors.view.fragments.messagesFragment

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentMessagesBinding
import com.example.dnmotors.utils.FileUtils
import com.example.dnmotors.view.adapter.MessagesAdapter
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.IOException
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.lifecycle.lifecycleScope
import com.example.dnmotors.utils.MultimediaUtils.encodeToBase64
import com.example.dnmotors.utils.MultimediaUtils.fileToBase64
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRef: CollectionReference

    private lateinit var carId: String
    private lateinit var dealerId: String

    private val mediaPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleSelectedMedia(it) }
        }

    private var latestTmpUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { handleCapturedImage(it) }
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startRecordingInternal()
            } else {
                Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    @Volatile
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    @Volatile
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

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
        binding.mediaButton.setOnClickListener { selectMedia() }
        binding.videoRecordButton.setOnClickListener { takePicture() } // <-- Hooked up to UI button
        setupAudioRecordButton()

        listenForMessages()
        return binding.root
    }


    private fun selectMedia() {
        mediaPickerLauncher.launch("*/*")
    }


    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val encodedMessage = encodeToBase64(messageText)
            val senderId = auth.currentUser?.uid ?: return
            val senderName = auth.currentUser?.displayName ?: "Unknown User"
            val timestamp = System.currentTimeMillis()
            val chatId = "${dealerId}_$senderId"
            val isNotificationSent = false

            val message = Message(
                id = timestamp.toString(),
                senderId = senderId,
                name = senderName,
                message = encodedMessage,
                messageType = "text",
                timestamp = timestamp,
                carId = carId,
                notificationSent = isNotificationSent
            )

            messagesRef.add(message)
                .addOnSuccessListener {
                    binding.messageInput.text.clear()
                    val chatMetadata = mapOf(
                        "userId" to senderId,
                        "dealerId" to dealerId,
                        "carId" to carId,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    FirebaseFirestore.getInstance()
                        .collection("chats")
                        .document(chatId)
                        .set(chatMetadata, SetOptions.merge())
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
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
            base64 = base64,
            messageType = mediaType,
            timestamp = System.currentTimeMillis(),
            carId = carId
        )

        messagesRef.add(message)
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send media", Toast.LENGTH_SHORT).show()
            }
    }

    private fun takePicture() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val tmpFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
        }

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tmpFile
        )
    }

    private fun handleCapturedImage(uri: Uri) {
        val path = FileUtils.getPath(requireContext(), uri)
        if (path != null) {
            val base64 = fileToBase64(path)
            if (base64.isNotEmpty()) {
                sendBase64Media(base64, "image")
            } else {
                Toast.makeText(context, "Failed to encode captured image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to access captured image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedMedia(uri: Uri) {
        val mediaType = requireContext().contentResolver.getType(uri)?.split("/")?.getOrNull(0)
        if (mediaType == null || mediaType != "image" && mediaType != "audio") {
            Toast.makeText(context, "Unsupported file type: $mediaType", Toast.LENGTH_SHORT).show()
            return
        }

        val path = FileUtils.getPath(requireContext(), uri)
        if (path != null) {
            val base64 = fileToBase64(path)
            if (base64.isNotEmpty()) {
                sendBase64Media(base64, mediaType)
            } else {
                Toast.makeText(context, "Failed to encode selected media", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to access selected media file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAudioRecordButton() {
        binding.audioRecordButton.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isRecording) return@setOnTouchListener true
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecordingInternal()
                    } else {
                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) stopRecordingAndSend() else cleanupRecorder()
                    true
                }
                else -> false
            }
        }
    }

    private fun startRecordingInternal() {
        if (isRecording) return

        val fileName = "recorded_audio_${System.currentTimeMillis()}.3gp"
        val file = File(requireContext().cacheDir, fileName)
        audioFilePath = file.absolutePath
        var recorder: MediaRecorder? = null

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            Toast.makeText(context, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
            recorder?.release()
            cleanupRecorder()
        }
    }

    private fun stopRecordingAndSend() {
        val recorder = mediaRecorder ?: return
        val path = audioFilePath ?: return

        try {
            recorder.stop()
            val base64 = fileToBase64(path)
            if (base64.isNotEmpty()) {
                sendBase64Media(base64, "audio")
            }
        } catch (e: Exception) {
            File(path).delete()
        } finally {
            cleanupRecorder(recorder)
        }
    }

    private fun cleanupRecorder(recorder: MediaRecorder? = mediaRecorder) {
        recorder?.release()
        mediaRecorder = null
        isRecording = false
        audioFilePath = null
    }

    private fun listenForMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load messages", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val message = doc.toObject(Message::class.java)
                    if (message != null && message.senderId != currentUserId && !message.notificationSent) {
                        messagesRef.document(doc.id).update("notificationSent", true)
                    }
                    message
                }.orEmpty()

                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }
}

