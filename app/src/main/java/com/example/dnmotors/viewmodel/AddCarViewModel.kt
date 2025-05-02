package com.example.dnmotors.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Car
import com.example.dnmotors.model.ImgurApiService
import com.example.domain.model.ImgurResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class AddCarViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    var successMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)


    fun addCar(car: Car, context: Context) {
        isLoading = true
        firestore.collection("Cars").document(car.id)
            .set(car)
            .addOnSuccessListener {
                successMessage = "Car added successfully"
                isLoading = false
                Toast.makeText(context, "Автомобиль добавлен", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                successMessage = "Failed to add car: ${it.message}"
                isLoading = false
                Toast.makeText(context, "Ошибка при добавлении автомобиля", Toast.LENGTH_SHORT).show()
            }
    }

    fun uploadMultipleImagesToImgur(
        context: Context,
        uris: List<Uri>,
        onComplete: (List<String>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val uploadedLinks = mutableListOf<String>()
            val jobs = uris.map { uri ->
                async {
                    suspendCancellableCoroutine<String?> { continuation ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            val imageBytes = outputStream.toByteArray()
                            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                            val retrofit = Retrofit.Builder()
                                .baseUrl("https://api.imgur.com/3/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()

                            val service = retrofit.create(ImgurApiService::class.java)
                            val call = service.uploadImage(base64Image)

                            call.enqueue(object : Callback<ImgurResponse> {
                                override fun onResponse(
                                    call: Call<ImgurResponse>,
                                    response: Response<ImgurResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        continuation.resume(response.body()?.data?.link, null)
                                    } else {
                                        continuation.resume(null, null)
                                    }
                                }

                                override fun onFailure(call: Call<ImgurResponse>, t: Throwable) {
                                    continuation.resume(null, null)
                                }
                            })
                        } catch (e: Exception) {
                            continuation.resume(null, null)
                        }
                    }
                }
            }

            val results = jobs.map { it.await() }.filterNotNull()

            withContext(Dispatchers.Main) {
                onComplete(results)
            }
        }
    }
}
