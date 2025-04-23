package com.example.dnmotors.viewdealer.compose.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.dnmotors.view.fragments.carFragment.Car
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.dnmotors.viewmodel.AddCarViewModel
import java.util.UUID
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.size
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AddCarScreen(viewModel: AddCarViewModel = viewModel()) {
    var vin by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val dealerId = FirebaseAuth.getInstance().currentUser?.uid

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Car", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN") })
        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") })
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") })
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

        Button(onClick = {
            if (dealerId != null) {
                val newCar = Car(
                    id = UUID.randomUUID().toString(),
                    vin = vin,
                    dealerId = dealerId,
                    brand = brand,
                    model = model,
                    year = year.toIntOrNull() ?: 0,
                    price = price.toIntOrNull() ?: 0,
                    phoneNumber = phoneNumber.toLongOrNull() ?: 0L
                )
                viewModel.addCar(newCar)
            } else {
                viewModel.successMessage = "You must be logged in to add a car."
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
