package com.example.dnmotors.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.source.AppDatabase
import com.example.domain.model.CarEntity
import com.example.data.repository.CarRepository
import com.example.dnmotors.App.Companion.context
import com.example.domain.model.Car
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarViewModel(private var repository: CarRepository) : ViewModel() {

    val allCars: LiveData<List<CarEntity>>

    init {
        allCars = repository.allCars
    }

    fun refreshCars(remoteCars: List<Car>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshCarsFromRemote(remoteCars)
        }
    }

    fun getCarByVin(vin: String, callback: (CarEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getCarByVin(vin)
            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }
}
