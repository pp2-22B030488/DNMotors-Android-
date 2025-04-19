package com.example.dnmotors.view.fragments.messagesFragment

import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val mediaPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadMedia(it) }
        }

    private lateinit var carId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        // Get nav args
        val args = MessagesFragmentArgs.fromBundle(requireArguments())
        carId = args.carId

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().getReference("messages")
            .child(userId).child(carId)

        adapter = MessagesAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener { sendMessage() }
        binding.mediaButton.setOnClickListener {
            mediaPickerLauncher.launch("*/*")
        }
        binding.audioRecordButton.setOnClickListener { startRecording() }
        binding.videoRecordButton.setOnClickListener { startVideoRecording() }

        listenForMessages()
        return binding.root
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val encodedMessage = encodeToBase64(messageText)
            val message = Message(
                id = auth.currentUser?.uid,
                name = auth.currentUser?.displayName ?: "Unknown",
                message = encodedMessage,
                timestamp = System.currentTimeMillis()
            )
            database.push().setValue(message)
            binding.messageInput.text.clear()
        }
    }

    private fun sendBase64Media(base64: String, mediaType: String) {
        val message = Message(
            id = auth.currentUser?.uid,
            name = auth.currentUser?.displayName ?: "Unknown",
            base64 = base64,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis()
        )
        database.push().setValue(message)
    }
    fun fileToBase64(path: String?): String {
        if (path == null) return ""
        val file = File(path)
        if (!file.exists()) return ""
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }



    private fun listenForMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (s in snapshot.children) {
                    val message = s.getValue(Message::class.java)
                    if (message != null) messages.add(message)
                }
                adapter.submitList(messages)
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun uploadMedia(uri: Uri) {
        val mediaType = requireContext().contentResolver.getType(uri)?.split("/")?.get(0) ?: "unknown"
        val storageRef = FirebaseStorage.getInstance().reference
            .child("media/${auth.currentUser?.uid}/${System.currentTimeMillis()}")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val message = Message(
                        id = auth.currentUser?.uid,
                        name = auth.currentUser?.displayName ?: "Unknown",
                        mediaUrl = downloadUri.toString(),
                        mediaType = mediaType,
                        timestamp = System.currentTimeMillis()
                    )
                    database.push().setValue(message)
                }
            }
    }

    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFilePath: String

    private fun startRecording() {
        val file = File(requireContext().cacheDir, "recorded_audio.3gp")
        audioFilePath = file.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        val base64 = fileToBase64(audioFilePath)
        sendBase64Media(base64, "audio")
    }

    private val videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            if (success && ::videoUri.isInitialized) {
                val path = FileUtils.getPath(requireContext(), videoUri)
                val base64 = fileToBase64(path)
                sendBase64Media(base64, "video")
            }
        }

    private lateinit var videoUri: Uri

    private fun startVideoRecording() {
        val file = File(requireContext().cacheDir, "recorded_video.mp4")
        videoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        videoCaptureLauncher.launch(videoUri)
    }

    fun encodeToBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }


}
