package com.example.dnmotors.viewdealer.repository

import com.example.dnmotors.view.fragments.carFragment.Car
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CarRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getCarsForDealer(dealerId: String): List<Car> {
        return try {
            db.collection("Cars")
                .whereEqualTo("dealerId", dealerId)
                .get()
                .await()
                .toObjects(Car::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}