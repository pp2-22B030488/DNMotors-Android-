package com.example.data.repository

import androidx.lifecycle.LiveData
import com.example.data.model.CarDao
import com.example.domain.model.Car
import com.example.domain.model.CarEntity

class CarRepository(private val carDao: CarDao) {
    val allCars: LiveData<List<CarEntity>> = carDao.getAllCars()

    suspend fun refreshCarsFromRemote(remoteCars: List<Car>) {
        val entities = remoteCars.map {
            CarEntity(
                id = it.id,
                vin = it.vin,
                dealerId = it.dealerId,
                brand = it.brand,
                model = it.model,
                year = it.year,
                generation = it.generation,
                price = it.price,
                transmission = it.transmission,
                driveType = it.driveType,
                fuelType = it.fuelType,
                location = it.location,
                description = it.description,
                previewUrl = it.previewUrl,
                imageUrl = it.imageUrl,
                phoneNumber = it.phoneNumber,
                bodyType = it.bodyType,
                engineCapacity = it.engineCapacity,
                isLiked = it.isLiked,
                mileageKm = it.mileageKm,
                condition = it.condition
            )
        }
        carDao.clearCars()
        carDao.insertCars(entities)
    }

    suspend fun getCarByVin(vin: String): CarEntity? = carDao.getCarByVin(vin)
}
