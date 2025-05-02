package com.example.dnmotors.viewdealer.compose.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dnmotors.R
import com.example.dnmotors.model.ImgurApiService
import com.example.domain.model.ImgurResponse
import com.example.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current

    var user by remember { mutableStateOf(User()) }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var backgroundUri by remember { mutableStateOf<Uri?>(null) }

    val launcherAvatar = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { avatarUri = it }
    }

    val launcherBackground = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { backgroundUri = it }
    }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val primaryRed = colorResource(id = R.color.primary_red)

    // Загрузка данных пользователя
    LaunchedEffect(Unit) {
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    user = document.toObject(User::class.java) ?: User()
                }
        }
    }

    fun uploadImageToImgur(uri: Uri, onResult: (String?) -> Unit) {
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
            override fun onResponse(call: Call<ImgurResponse>, response: Response<ImgurResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body()?.data?.link)
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<ImgurResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun saveChanges() {
        if (userId == null) return

        val updates = hashMapOf<String, Any>(
            "name" to user.name,
            "location" to user.location,
            "phoneNumber" to user.phoneNumber,
        )

        fun uploadAndSave(uri: Uri, fieldName: String) {
            uploadImageToImgur(uri) { link ->
                if (!link.isNullOrEmpty()) {
                    firestore.collection("users").document(userId).update(fieldName, link)
                }
            }
        }

        avatarUri?.let { uploadAndSave(it, "avatarUrl") }
        backgroundUri?.let { uploadAndSave(it, "profileFon") }

        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Профиль обновлён", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------- UI -------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {

        // Box с аватаркой и фоном
        Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {

            // Фон профиля
            if (backgroundUri != null) {
                AsyncImage(
                    model = backgroundUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { launcherBackground.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else if (!user.profileFon.isNullOrBlank()) {
                AsyncImage(
                    model = user.profileFon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { launcherBackground.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_fon),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { launcherBackground.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            }

            // Кнопка назад
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }

            // Аватарка и текст
            Column(modifier = Modifier.align(Alignment.Center)) {

                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable { launcherAvatar.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable { launcherAvatar.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable { launcherAvatar.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Edit your Profile",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        // Карточка с полями
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = user.name,
                    onValueChange = { user = user.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = user.location,
                    onValueChange = { user = user.copy(location = it) },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = user.phoneNumber,
                    onValueChange = { user = user.copy(phoneNumber = it) },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Кнопка Save Changes
        Button(
            onClick = { saveChanges() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("SAVE CHANGES", color = Color.White)
        }
    }
}
