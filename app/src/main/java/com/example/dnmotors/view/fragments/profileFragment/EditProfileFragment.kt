package com.example.dnmotors.view.fragments.profileFragment

import android.graphics.Bitmap
import com.example.dnmotors.model.ImgurApiService
import com.example.dnmotors.model.ImgurResponse
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dnmotors.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE_AVATAR = 1
    private val PICK_IMAGE_BACKGROUND = 2

    private var selectedAvatarUri: Uri? = null
    private var selectedBackgroundUri: Uri? = null

    private lateinit var imgurService: ImgurApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        setupRetrofit()

        binding.ivProfileEdit.setOnClickListener {
            openGallery(PICK_IMAGE_AVATAR)
        }

        binding.ivBackgroundEdit.setOnClickListener {
            openGallery(PICK_IMAGE_BACKGROUND)
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val avatarUrl = document.getString("avatarUrl")
                    val backgroundUrl = document.getString("profileFon")
                    val name = document.getString("name")
                    val location = document.getString("location")
                    val phone = document.getString("phoneNumber")

                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this).load(avatarUrl).into(binding.ivProfileEdit)
                    }

                    if (!backgroundUrl.isNullOrEmpty()) {
                        Glide.with(this).load(backgroundUrl).into(binding.ivBackgroundEdit)
                    }

                    binding.etName.setText(name ?: "")
                    binding.etLocation.setText(location ?: "")
                    binding.etPhoneNumber.setText(phone ?: "")
                }
            }
    }


    private fun setupRetrofit() {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgur.com/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        imgurService = retrofit.create(ImgurApiService::class.java)
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data

            when (requestCode) {
                PICK_IMAGE_AVATAR -> {
                    selectedAvatarUri = selectedImageUri
                    Glide.with(this).load(selectedAvatarUri).centerCrop().into(binding.ivProfileEdit)
                }
                PICK_IMAGE_BACKGROUND -> {
                    selectedBackgroundUri = selectedImageUri
                    Glide.with(this).load(selectedBackgroundUri).into(binding.ivBackgroundEdit)
                }
            }
        }
    }

    private fun saveProfile() {
        if (selectedAvatarUri != null) {
            uploadImageToImgur(selectedAvatarUri!!, "avatarUrl")
        }
        if (selectedBackgroundUri != null) {
            uploadImageToImgur(selectedBackgroundUri!!, "profileFon")
        }

    }

    private fun uploadImageToImgur(uri: Uri, field: String) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val call = imgurService.uploadImage(base64Image)
        call.enqueue(object : Callback<ImgurResponse> {
            override fun onResponse(call: Call<ImgurResponse>, response: Response<ImgurResponse>) {
                if (response.isSuccessful) {
                    val imgurLink = response.body()?.data?.link
                    saveLinkToFirestore(imgurLink, field)
                } else {
                    context?.let {
                        Toast.makeText(it, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ImgurResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLinkToFirestore(link: String?, field: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>()
        link?.let {
            updates[field] = it
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                context?.let {
                    Toast.makeText(it, "Профиль обновлен!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                context?.let {
                    Toast.makeText(it, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
