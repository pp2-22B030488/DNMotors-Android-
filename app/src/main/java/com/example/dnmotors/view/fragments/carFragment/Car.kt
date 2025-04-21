package com.example.dnmotors.view.fragments.carFragment
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car(
    val vin: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val price: Int = 0,
    val mileageKm: Int = 0,
    val transmission: String = "",
    val driveType: String = "",
    val fuelType: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: List<String> = emptyList(),
    val condition: String = "",
    val phoneNumber: Long = 0L,
    var isLiked: Boolean = false

) : Parcelable

