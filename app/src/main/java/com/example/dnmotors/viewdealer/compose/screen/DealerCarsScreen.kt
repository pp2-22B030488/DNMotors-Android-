package com.example.dnmotors.viewdealer.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.domain.model.Car
import com.example.domain.model.User
import com.example.dnmotors.viewdealer.repository.CarRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun DealerCarsScreen(navController: NavHostController, paddingValues: PaddingValues) {
    val cars = remember { mutableStateListOf<Car>() }
    val user = remember { mutableStateOf(User()) }

    val showDialog = remember { mutableStateOf(false) }
    val selectedCar = remember { mutableStateOf<Car?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val dealerId = FirebaseAuth.getInstance().currentUser?.uid
        if (dealerId != null) {
            val repo = CarRepository()
            val dealerCars = repo.getCarsForDealer(dealerId)
            cars.clear()
            cars.addAll(dealerCars)

            val dealerInfo = repo.getDealerInfo(dealerId)
            dealerInfo?.let {
                user.value = it
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Hi Dealer!", style = MaterialTheme.typography.headlineSmall)
                    Text(text = "Welcome back to your panel.", style = MaterialTheme.typography.bodyMedium)
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB30000)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F6F5))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = user.value.company,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFB30000)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No. Of Cars : ${cars.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB30000)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cars List",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(cars) { car ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                color = Color(0xFFE8F6F5),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (car.imageUrl.isNotEmpty() && car.imageUrl.first().isNotBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(car.imageUrl.first()),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Gray, shape = MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${car.brand} ${car.model}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFB30000)
                            )
                            Text(
                                text = "${car.generation} ${car.year}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = {
                            navController.navigate("dealer_car_details/${car.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFB30000))
                        }
                        IconButton(onClick = {
                            selectedCar.value = car
                            showDialog.value = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFB30000))
                        }
                    }
                }
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog.value = false
                        selectedCar.value = null
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val carToDelete = selectedCar.value
                            if (carToDelete != null) {
                                cars.remove(carToDelete)

                                scope.launch {
                                    val repo = CarRepository()
                                    val deleted = repo.deleteCar(carToDelete.id)
                                    if (!deleted) {
                                        // TODO: Показать Snackbar/Toast
                                    }
                                }
                            }
                            showDialog.value = false
                            selectedCar.value = null
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog.value = false
                            selectedCar.value = null
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Confirm Deletion")
                    },
                    text = {
                        Text("Are you sure you want to delete this car?")
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("add_car") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFFB30000)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Car", tint = Color.White)
        }
    }
}