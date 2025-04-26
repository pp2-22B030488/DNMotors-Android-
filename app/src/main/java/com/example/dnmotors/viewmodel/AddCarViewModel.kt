package com.example.dnmotors.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.model.Car
import com.example.dnmotors.viewdealer.repository.CarRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue

class AddCarViewModel(private val repository: CarRepository = CarRepository()) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var successMessage by mutableStateOf<String?>(null)

    fun addCar(car: Car) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.addCar(car)
            successMessage = if (result) "Car added successfully!" else "Failed to add car."
            isLoading = false
        }
    }
}
