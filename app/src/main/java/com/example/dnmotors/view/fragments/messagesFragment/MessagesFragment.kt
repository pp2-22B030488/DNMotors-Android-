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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.IOException
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var carId: String

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

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()

            return binding.root
        }
        database = FirebaseDatabase.getInstance().getReference("messages")
            .child(userId).child(carId)

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

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            Log.w("MessagesFragment", "Fragment stopped during recording. Cleaning up recorder.")
            cleanupRecorder()
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val encodedMessage = encodeToBase64(messageText)
            val message = Message(
                id = System.currentTimeMillis().toString(),
                senderId = auth.currentUser?.uid,
                name = auth.currentUser?.displayName ?: "Unknown User",
                message = encodedMessage,
                messageType = "text",
                timestamp = System.currentTimeMillis()
            )
            database.push().setValue(message)
                .addOnSuccessListener {
                    binding.messageInput.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                    Log.e("MessagesFragment", "Failed to send message", it)
                }
        }
    }

    private fun sendBase64Media(base64: String, mediaType: String) {
        if (base64.isEmpty()) {
            Log.e("MessagesFragment", "Attempted to send empty Base64 string for type: $mediaType")
            Toast.makeText(context, "Failed to process media", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            id = System.currentTimeMillis().toString(),
            senderId = auth.currentUser?.uid,
            name = auth.currentUser?.displayName ?: "Unknown User",
            base64 = base64,
            messageType = mediaType,
            timestamp = System.currentTimeMillis()
        )
        database.push().setValue(message)
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send media message", Toast.LENGTH_SHORT).show()
                Log.e("MessagesFragment", "Failed to send media message", it)
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (s in snapshot.children) {
                    val message = s.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                messages.sortBy { it.timestamp }
                adapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesFragment", "Firebase listener cancelled", error.toException())
                Toast.makeText(context, "Failed to load messages: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
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
