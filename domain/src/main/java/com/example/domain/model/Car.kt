package com.example.domain.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car(
    val id: String = "",
    val vin: String = "",
    val dealerId: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val generation: String = "",
    val price: Int = 0,
    val transmission: String = "",
    val driveType: String = "",
    val fuelType: String = "",
    val location: String = "",
    val description: String = "",
    val previewUrl: String = "",
    val imageUrl: List<String> = emptyList(),
    val phoneNumber: Long = 0L,
    val bodyType: String = "",
    val engineCapacity: String = "",
    var isLiked: Boolean = false,
    val mileageKm: Int = 0,
    val condition: String = "",
) : Parcelable

