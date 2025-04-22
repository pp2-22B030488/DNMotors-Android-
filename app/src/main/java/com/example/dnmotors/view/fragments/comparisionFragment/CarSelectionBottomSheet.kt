package com.example.dnmotors.view.fragments.comparisionFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.BottomSheetCarSelectionBinding
import com.example.dnmotors.view.fragments.carFragment.Car
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class CarSelectionBottomSheet(
    private val onCarSelected: (Car) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var carAdapter: CarSelectAdapter
    private lateinit var binding: BottomSheetCarSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCarSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = FirebaseFirestore.getInstance()
        carAdapter = CarSelectAdapter { selectedCar ->
            onCarSelected(selectedCar)
            dismiss()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        fetchCarsFromFirestore()
    }

    private fun fetchCarsFromFirestore() {
        db.collection("Cars").get()
            .addOnSuccessListener { result ->
                val cars = result.mapNotNull { it.toObject(Car::class.java) }
                Log.d("CarSelection", "Fetched cars: ${cars.size}")
                carAdapter.submitList(cars)
            }
            .addOnFailureListener { exception ->
                Log.e("CarSelection", "Error fetching cars", exception)
            }
    }

}
