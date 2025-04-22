package com.example.dnmotors.viewdealer.repository

import com.example.dnmotors.view.fragments.carFragment.Car
import com.example.dnmotors.view.fragments.profileFragment.User
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
    suspend fun getDealerInfo(dealerId: String): User? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(dealerId)
                .get()
                .await()

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    suspend fun deleteCar(carId: String): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("cars")
                .document(carId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }



}