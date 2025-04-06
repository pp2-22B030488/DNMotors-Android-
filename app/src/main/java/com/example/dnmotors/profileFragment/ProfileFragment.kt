package com.example.dnmotors.profileFragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storageRef = FirebaseStorage.getInstance().reference

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.settings_menu)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.btnSettings -> {
                    findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
                    true
                }
                else -> false
            }
        }
        loadUserName()
        loadUserData()

        binding.avatarChange.setOnClickListener {
            openFileChooser()
        }
    }
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            uploadImage()
        }
    }
    private fun uploadImage() {
        val userId = auth.currentUser?.uid ?: return
        val fileRef = storageRef.child("avatars/$userId.jpg")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("avatarUrl", imageUrl)
            .addOnSuccessListener {
                loadUserData()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show()
            }
    }
    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    binding.userName.text = name
                    val avatarUrl = document.getString("avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl).into(binding.ivProfile)
                    }
                }
            }
            .addOnFailureListener {
                binding.userName.text = "Error loading name"
            }
    }
    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "No Name"
                        binding.userName.text = name
                    }
                }
                .addOnFailureListener {
                    binding.userName.text = "Error loading name"
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

