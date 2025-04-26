package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dnmotors.model.Car
import com.example.dnmotors.viewmodel.AddCarViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.util.*

@Composable
fun AddCarScreen(viewModel: AddCarViewModel = viewModel()) {
    var carFromJson by remember { mutableStateOf<Car?>(null) }

    var vin by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var showJsonInput by remember { mutableStateOf(false) }
    var jsonInput by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }

    val dealerId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Делаем прокрутку
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Car", style = MaterialTheme.typography.headlineMedium)

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
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Button(onClick = { showJsonInput = !showJsonInput }) {
            Text(if (showJsonInput) "Скрыть ввод JSON" else "Заполнить из JSON")
        }

        if (showJsonInput) {
            OutlinedTextField(
                value = jsonInput,
                onValueChange = { jsonInput = it },
                label = { Text("Вставьте сюда JSON") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Можно даже сделать побольше
            )

            jsonError?.let {
                Text(it, color = Color.Red)
            }
        }

        Button(onClick = {
            if (dealerId != null) {
                // Пытаемся парсить JSON перед добавлением
                if (jsonInput.isNotBlank()) {
                    try {
                        val parsedCar = Gson().fromJson(jsonInput, Car::class.java)
                        carFromJson = parsedCar
                        jsonError = null
                    } catch (e: Exception) {
                        jsonError = "Ошибка разбора JSON: ${e.localizedMessage}"
                        return@Button // Если ошибка, выходим, не сохраняем
                    }
                }

                val newCar = if (carFromJson != null) {
                    Car(
                        id = UUID.randomUUID().toString(),
                        vin = carFromJson!!.vin.trim(),
                        dealerId = dealerId,
                        brand = carFromJson!!.brand.trim(),
                        model = carFromJson!!.model.trim(),
                        year = carFromJson!!.year,
                        price = carFromJson!!.price,
                        phoneNumber = carFromJson!!.phoneNumber
                    )
                } else {
                    Car(
                        id = UUID.randomUUID().toString(),
                        vin = vin.trim(),
                        dealerId = dealerId,
                        brand = brand.trim(),
                        model = model.trim(),
                        year = year.toIntOrNull() ?: 0,
                        price = price.toIntOrNull() ?: 0,
                        phoneNumber = phoneNumber.toLongOrNull() ?: 0L
                    )
                }
                viewModel.addCar(newCar)
                carFromJson = null // обнуляем после добавления
            } else {
                viewModel.successMessage = "Вы должны быть авторизованы для добавления машины."
            }
        }) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Add Car")
            }
        }

        viewModel.successMessage?.let {
            Text(it, color = if (it.contains("success")) Color.Green else Color.Red)
        }
    }
}
