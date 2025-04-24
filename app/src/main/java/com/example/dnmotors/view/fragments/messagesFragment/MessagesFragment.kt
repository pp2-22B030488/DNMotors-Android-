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
            latestTmpUri?.let { uri ->
                 Log.d("MessagesFragment", "Picture taken successfully: $uri")
                 handleCapturedImage(uri)
            }
        } else {
             Log.e("MessagesFragment", "Picture capture failed or was cancelled.")
             Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            if (success && ::videoUri.isInitialized) {
                val path = FileUtils.getPath(requireContext(), videoUri)
                if (path != null) {
                    val base64 = fileToBase64(path)
                    if (base64.isNotEmpty()) {
                        sendBase64Media(base64, "video")
                    } else {
                        Toast.makeText(context, "Failed to encode video", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to get video path", Toast.LENGTH_SHORT).show()
                }
            } else if (!success) {
                Toast.makeText(context, "Video capture failed or cancelled", Toast.LENGTH_SHORT).show()
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

    private val requestVideoPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startVideoRecordingInternal()
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    @Volatile
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    @Volatile
    private var isRecording = false
    private lateinit var videoUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        val args = MessagesFragmentArgs.fromBundle(requireArguments())
        carId = args.carId
        dealerId = args.dealerId // Make sure you pass this from navigation args

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
        setupAudioRecordButton()
        binding.videoRecordButton.setOnClickListener { startVideoRecording() }

        listenForMessages()
        return binding.root
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
                isNotificationSent = false // Add this line

            )

            // Save the message
            messagesRef.add(message)
                .addOnSuccessListener {
                    binding.messageInput.text.clear()

                    // Update chat metadata
                    val chatMetadata = mapOf(
                        "userId" to senderId,
                        "dealerId" to dealerId,
                        "carId" to carId,
                        "lastMessage" to messageText,
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "isNotificationSent" to isNotificationSent
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

    private fun selectMedia() {
        mediaPickerLauncher.launch("*/*")
    }

    private fun takePicture() {
        lifecycleScope.launch {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                Log.d("MessagesFragment", "Launching camera with temp URI: $uri")
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
        Log.d("MessagesFragment", "Selected media type: $mediaType, URI: $uri")

        if (mediaType == null || !listOf("audio", "video", "image").contains(mediaType)) {
            Toast.makeText(context, "Unsupported file type: $mediaType", Toast.LENGTH_SHORT).show()
            Log.w("MessagesFragment", "Unsupported file type selected: $mediaType")
            return
        }

        val path = FileUtils.getPath(requireContext(), uri)
        if (path != null) {

            val base64 = fileToBase64(path)
            if (base64.isNotEmpty()) {
                val finalMediaType = when(mediaType) {
                    "audio" -> "audio"
                    "video" -> "video"
                    "image" -> "image"
                    else -> "unknown"
                }
                if (finalMediaType != "unknown") {
                    sendBase64Media(base64, finalMediaType)
                } else {
                    Toast.makeText(context, "Cannot determine media type", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to encode selected media", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to access selected media file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupAudioRecordButton() {
        binding.audioRecordButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecordingInternal()
                    } else {
                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) {
                        stopRecordingAndSend()
                    } else {
                        cleanupRecorder()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startRecordingInternal() {
        if (isRecording) {
            Log.w("MessagesFragment", "startRecordingInternal called while already recording.")
            return
        }

        val fileName = "recorded_audio_${System.currentTimeMillis()}.3gp"
        val file = File(requireContext().cacheDir, fileName)
        audioFilePath = file.absolutePath
        var recorder: MediaRecorder? = null

        try {
            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            this.mediaRecorder = recorder
            isRecording = true
            Log.d("MessagesFragment", "Audio recording started: $audioFilePath")

        } catch (e: Exception) {
            Log.e("MessagesFragment", "MediaRecorder setup or start failed", e)
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
            recorder?.release()
            cleanupRecorder()
        }
    }

    private fun stopRecordingAndSend() {
        if (!isRecording || mediaRecorder == null) {
            Log.w("MessagesFragment", "stopRecordingAndSend called but not in a valid recording state.")
            cleanupRecorder()
            return
        }

        val recorderToStop = mediaRecorder
        val currentFilePath = audioFilePath

        isRecording = false
        mediaRecorder = null

        var stopSucceeded = false
        try {
            Log.d("MessagesFragment", "Attempting to stop audio recording...")
            recorderToStop?.stop()
            stopSucceeded = true
            Log.d("MessagesFragment", "Audio recording stopped successfully.")

        } catch (e: RuntimeException) {
            Log.e("MessagesFragment", "Error stopping MediaRecorder", e)
            Toast.makeText(context, "Error stopping recording", Toast.LENGTH_SHORT).show()
            currentFilePath?.let { path ->
                try {
                    File(path).delete()
                    Log.d("MessagesFragment", "Deleted potentially corrupted audio file: $path")
                } catch (ioe: IOException) {
                    Log.e("MessagesFragment", "Error deleting corrupted audio file", ioe)
                }
            }
        } finally {
            cleanupRecorder(recorderToStop)
        }

        if (stopSucceeded && currentFilePath != null) {
            val base64 = fileToBase64(currentFilePath)
            if (base64.isNotEmpty()) {
                sendBase64Media(base64, "audio")
            } else {
                Toast.makeText(context, "Failed to encode audio", Toast.LENGTH_SHORT).show()
                try { File(currentFilePath).delete() } catch (e: Exception) {}
            }
        } else if (stopSucceeded && currentFilePath == null) {
            Log.e("MessagesFragment", "Stop succeeded but audio file path was null.")
            Toast.makeText(context, "Audio file not found after recording", Toast.LENGTH_SHORT).show()
        }
    }


    private fun cleanupRecorder(recorderInstance: MediaRecorder? = this.mediaRecorder) {
        Log.d("MessagesFragment", "cleanupRecorder called.")
        recorderInstance?.let {
            try {
                it.release()
                Log.d("MessagesFragment", "MediaRecorder released.")
            } catch (e: Exception) {
                Log.e("MessagesFragment", "Error releasing MediaRecorder during cleanup", e)
            }
        }

        if (recorderInstance == this.mediaRecorder || this.mediaRecorder != null) {
            this.mediaRecorder = null
        }
        isRecording = false
        audioFilePath = null

        if (view != null) {
            try {
            } catch (e: IllegalStateException) {
                Log.w("MessagesFragment", "Could not reset button text, view likely destroyed.")
            }
        }
        Log.d("MessagesFragment", "Recorder cleanup complete.")
    }


    private fun startVideoRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startVideoRecordingInternal()
        } else {
            requestVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startVideoRecordingInternal() {
        val fileName = "recorded_video_${System.currentTimeMillis()}.mp4"
        val file = File(requireContext().cacheDir, fileName)
        try {
            videoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            Log.d("MessagesFragment", "Launching video capture intent with URI: $videoUri")
            videoCaptureLauncher.launch(videoUri)
        } catch (e: IllegalArgumentException) {
            Log.e("MessagesFragment", "FileProvider failed for video recording.", e)
            Toast.makeText(context, "Error setting up video recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForMessages() {
        messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessagesFragment", "Firestore listen failed", error)
                    Toast.makeText(context, "Failed to load messages", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                }.orEmpty()

                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }
    private fun fileToBase64(path: String?): String {
        if (path == null) {
            Log.e("MessagesFragment", "fileToBase64: Input path is null")
            return ""
        }
        val file = File(path)
        if (!file.exists()) {
            Log.e("MessagesFragment", "fileToBase64: File does not exist at path: $path")
            return ""
        }
        val fileSizeInMB = file.length() / (1024.0 * 1024.0)
        val maxSizeMB = 1000
        if (fileSizeInMB > maxSizeMB) {
            Log.e("MessagesFragment", "File size (${fileSizeInMB}MB) exceeds limit (${maxSizeMB}MB) for Base64 encoding.")
            Toast.makeText(context, "Selected file is too large (Max ${maxSizeMB}MB)", Toast.LENGTH_SHORT).show()
            return ""
        }

        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: IOException) {
            Log.e("MessagesFragment", "Failed to read file for Base64 encoding", e)
            ""
        } catch (e: OutOfMemoryError) {
            Log.e("MessagesFragment", "OutOfMemoryError encoding file to Base64. File might be too large.", e)
            Toast.makeText(context, "File is too large to process", Toast.LENGTH_SHORT).show()
            ""
        }
    }

    fun encodeToBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
}
