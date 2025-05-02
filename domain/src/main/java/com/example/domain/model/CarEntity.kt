package com.example.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey val vin: String,
    val id: String,
    val dealerId: String,
    val brand: String,
    val model: String,
    val year: Int,
    val generation: String,
    val price: Int,
    val transmission: String,
    val driveType: String,
    val fuelType: String,
    val location: String,
    val description: String,
    val previewUrl: String,
    val imageUrl: List<String>,
    val phoneNumber: Long,
    val bodyType: String,
    val engineCapacity: String,
    val isLiked: Boolean,
    val mileageKm: Int,
    val condition: String
)
