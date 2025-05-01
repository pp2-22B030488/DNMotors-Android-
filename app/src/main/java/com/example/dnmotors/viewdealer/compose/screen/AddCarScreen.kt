package com.example.dnmotors.viewdealer.compose.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dnmotors.R
import com.example.dnmotors.model.Car
import com.example.dnmotors.viewmodel.AddCarViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(viewModel: AddCarViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    var carFromJson by remember { mutableStateOf<Car?>(null) }

    var vin by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var generation by remember { mutableStateOf("") }
    var transmission by remember { mutableStateOf("") }
    var driveType by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf("") }
    var engineCapacity by remember { mutableStateOf("") }
    var mileageKm by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }

    var showJsonInput by remember { mutableStateOf(false) }
    var jsonInput by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }

    val dealerId = FirebaseAuth.getInstance().currentUser?.uid
    val dealerPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
    val imageUris = remember { mutableStateListOf<Uri>() }

    val primaryRed = colorResource(id = R.color.primary_red)


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUris.add(it) }
    }
    val clearFields = {
        vin = ""
        brand = ""
        model = ""
        year = ""
        price = ""
        generation = ""
        transmission = ""
        driveType = ""
        fuelType = ""
        location = ""
        description = ""
        bodyType = ""
        engineCapacity = ""
        mileageKm = ""
        condition = ""
        imageUris.clear()
        jsonInput = ""
        carFromJson = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Car") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // светло-серый фон
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {

                    // Две колонки с полями
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN") })
                            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") })
                            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") })
                            OutlinedTextField(
                                value = year,
                                onValueChange = { year = it },
                                label = { Text("Year") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(value = generation, onValueChange = { generation = it }, label = { Text("Generation") })
                            OutlinedTextField(value = transmission, onValueChange = { transmission = it }, label = { Text("Transmission") })
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = driveType, onValueChange = { driveType = it }, label = { Text("Drive Type") })
                            OutlinedTextField(value = fuelType, onValueChange = { fuelType = it }, label = { Text("Fuel Type") })
                            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                            OutlinedTextField(value = condition, onValueChange = { condition = it }, label = { Text("Condition") })
                            OutlinedTextField(value = bodyType, onValueChange = { bodyType = it }, label = { Text("Body Type") })
                            OutlinedTextField(value = engineCapacity, onValueChange = { engineCapacity = it }, label = { Text("Engine Capacity") })
                            OutlinedTextField(
                                value = mileageKm,
                                onValueChange = { mileageKm = it },
                                label = { Text("Mileage (km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp)
                    )


                }
            }

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .height(48.dp)
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(20.dp))
                    .background(Color.White, shape = RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить фото",
                    tint = primaryRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add photos", color = primaryRed)
            }



            if (imageUris.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(imageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
            // Если carFromJson содержит imageUrl, показываем их тоже
            if (carFromJson?.imageUrl?.isNotEmpty() == true) {
                Text(
                    text = "Images from JSON:",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(carFromJson!!.imageUrl) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }


            Button(
                onClick = { showJsonInput = !showJsonInput },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                    Text(if (showJsonInput) "Hide JSON input" else "Fill from JSON")
            }


            if (showJsonInput) {
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    label = { Text("Insert JSON here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)

                )
                jsonError?.let {
                    Text(it, color = Color.Red)
                }
            }

            // КНОПКА ДОБАВИТЬ АВТО — всегда видимая
            Button(
                onClick = {
                    if (dealerId != null) {
                        Firebase.firestore.collection("users")
                            .document(dealerId)
                            .get()
                            .addOnSuccessListener { document ->
                                val phoneNumberStr = document.getString("phoneNumber") ?: ""
                                val phoneNumberLong =
                                    phoneNumberStr.filter { it.isDigit() }.toLongOrNull() ?: 0L

                                if (jsonInput.isNotBlank()) {
                                    try {
                                        val parsedCar = Gson().fromJson(jsonInput, Car::class.java)
                                        carFromJson = parsedCar.copy(
                                            dealerId = dealerId,
                                            phoneNumber = phoneNumberLong,
                                            id = UUID.randomUUID().toString()
                                        )
                                    } catch (e: Exception) {
                                        jsonError = "Ошибка разбора JSON: ${e.localizedMessage}"
                                        return@addOnSuccessListener
                                    }
                                }

                                viewModel.uploadMultipleImagesToImgur(context, imageUris) { links ->
                                    val newCar = (carFromJson ?: Car(
                                        id = UUID.randomUUID().toString(),
                                        vin = vin.trim(),
                                        dealerId = dealerId,
                                        brand = brand.trim(),
                                        model = model.trim(),
                                        year = year.toIntOrNull() ?: 0,
                                        generation = generation.trim(),
                                        price = price.toIntOrNull() ?: 0,
                                        transmission = transmission.trim(),
                                        driveType = driveType.trim(),
                                        fuelType = fuelType.trim(),
                                        location = location.trim(),
                                        description = description.trim(),
                                        phoneNumber = phoneNumberLong,
                                        bodyType = bodyType.trim(),
                                        engineCapacity = engineCapacity.trim(),
                                        mileageKm = mileageKm.toIntOrNull() ?: 0,
                                        condition = condition.trim()
                                    )).copy(
                                        imageUrl = (carFromJson?.imageUrl ?: emptyList()) + links,
                                        dealerId = dealerId,
                                        id = UUID.randomUUID().toString(),
                                    )
                                    viewModel.addCar(newCar, context)
                                    viewModel.isLoading = false
                                    clearFields()

                                }}
                                    .addOnFailureListener {
                                        Log.e("AddCarScreen", "Не удалось получить номер телефона дилера", it)
                                    }

                    } else {
                        viewModel.successMessage = "Вы должны быть авторизованы для добавления машины."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add Car")
                }
            }

            viewModel.successMessage?.let {
                Text(it, color = if (it.contains("success", ignoreCase = true)) Color.Green else Color.Red)
            }

            Spacer(modifier = Modifier.height(90.dp))

        }
    }



}
