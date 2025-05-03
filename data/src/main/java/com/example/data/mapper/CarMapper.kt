package com.example.data.mapper

import com.example.domain.model.Car
import com.example.domain.model.CarEntity

fun CarEntity.toCar(): Car {
    return Car(
        id, vin, dealerId, brand, model, year, generation, price, transmission,
        driveType, fuelType, location, description, previewUrl, imageUrl,
        phoneNumber, bodyType, engineCapacity, isLiked, mileageKm, condition
    )
}
