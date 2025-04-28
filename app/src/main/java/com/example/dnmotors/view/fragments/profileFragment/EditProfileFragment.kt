package com.example.dnmotors.view.fragments.profileFragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dnmotors.databinding.FragmentEditProfileBinding
import com.example.dnmotors.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private var avatarUri: Uri? = null
    private var backgroundUri: Uri? = null

    private val PICK_AVATAR_REQUEST = 1
    private val PICK_BACKGROUND_REQUEST = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        loadUserProfile()

        binding.ivProfileEdit.setOnClickListener {
            pickImage(PICK_AVATAR_REQUEST)
        }

        binding.ivBackgroundEdit.setOnClickListener {
            pickImage(PICK_BACKGROUND_REQUEST)
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        binding.etName.setText(it.name)
                        binding.etEmail.setText(it.email)
                        binding.etLocation.setText(it.location)
                        binding.etPhoneNumber.setText(it.phoneNumber)

                        Glide.with(this)
                            .load(it.avatarUrl)
                            .placeholder(com.example.dnmotors.R.drawable.profile_picture)
                            .into(binding.ivProfileEdit)

                        Glide.with(this)
                            .load(it.profileFon)
                            .placeholder(com.example.dnmotors.R.drawable.profile_fon)
                            .into(binding.ivBackgroundEdit)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                PICK_AVATAR_REQUEST -> {
                    avatarUri = data.data
                    binding.ivProfileEdit.setImageURI(avatarUri)
                }
                PICK_BACKGROUND_REQUEST -> {
                    backgroundUri = data.data
                    binding.ivBackgroundEdit.setImageURI(backgroundUri)
                }
            }
        }
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "name" to binding.etName.text.toString(),
            "email" to binding.etEmail.text.toString(),
            "location" to binding.etLocation.text.toString(),
            "phoneNumber" to binding.etPhoneNumber.text.toString()
        )

        // Сначала загружаем изображения, если выбраны
        uploadImages { avatarUrl, backgroundUrl ->
            avatarUrl?.let { updates["avatarUrl"] = it }
            backgroundUrl?.let { updates["profileFon"] = it }

            firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImages(onComplete: (avatarUrl: String?, backgroundUrl: String?) -> Unit) {
        val uploadedUrls = mutableMapOf<String, String?>()

        if (avatarUri != null) {
            val avatarRef = storage.reference.child("avatars/${UUID.randomUUID()}")
            avatarRef.putFile(avatarUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    avatarRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    uploadedUrls["avatarUrl"] = uri.toString()
                    checkUploadsComplete(uploadedUrls, onComplete)
                }
        } else {
            uploadedUrls["avatarUrl"] = null
            checkUploadsComplete(uploadedUrls, onComplete)
        }

        if (backgroundUri != null) {
            val backgroundRef = storage.reference.child("backgrounds/${UUID.randomUUID()}")
            backgroundRef.putFile(backgroundUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    backgroundRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    uploadedUrls["profileFon"] = uri.toString()
                    checkUploadsComplete(uploadedUrls, onComplete)
                }
        } else {
            uploadedUrls["profileFon"] = null
            checkUploadsComplete(uploadedUrls, onComplete)
        }
    }

    private fun checkUploadsComplete(
        uploadedUrls: Map<String, String?>,
        onComplete: (avatarUrl: String?, backgroundUrl: String?) -> Unit
    ) {
        if (uploadedUrls.size == 2) {
            onComplete(uploadedUrls["avatarUrl"], uploadedUrls["profileFon"])
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
