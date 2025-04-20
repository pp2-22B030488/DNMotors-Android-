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
//    val bodyType: String = "",
//    val engineVolume: String = "",
//    val horsepower: String = "",
//    val steeringWheel: String = "",
//    val color: String = "",
//    val ownersCount: Int = 0,
//    val exteriorFeatures: List<String> = emptyList(),
//    val interiorFeatures: List<String> = emptyList(),
//    val additionalFeatures: List<String> = emptyList()
    val phoneNumber: Long = 0L,
    var isLiked: Boolean = false

) : Parcelable

