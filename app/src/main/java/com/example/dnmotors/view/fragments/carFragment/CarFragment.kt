package com.example.dnmotors.view.fragments.carFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarBinding
import com.example.domain.model.Car
import com.example.dnmotors.view.adapter.CarAdapter
import com.example.dnmotors.view.fragments.favouritesFragment.FavouritesManager
import com.google.firebase.firestore.FirebaseFirestore

class CarFragment : Fragment() {

    private var _binding: FragmentCarBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarAdapter
    private val carList = mutableListOf<Car>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = CarAdapter(carList, { car -> showCarDetails(car) })
        recyclerView.adapter = adapter

        fetchCarsFromFirestore()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCredit = view.findViewById<ImageView>(R.id.btnCredit)

        btnCredit.setOnClickListener {
            findNavController().navigate(R.id.action_carFragment_to_carCreditFragment)
        }
        binding.analysisButton.setOnClickListener {
            findNavController().navigate(R.id.action_carFragment_to_carComparisionFragment)
        }
    }

    private fun fetchCarsFromFirestore() {
        db.collection("Cars")
            .get()
            .addOnSuccessListener { result ->
                carList.clear()
                for (document in result) {
                    val car = document.toObject(Car::class.java)
                    car.isLiked = FavouritesManager.likedCars.any { it.vin == car.vin }
                    carList.add(car)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load cars", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCarDetails(car: Car) {
        val bundle = Bundle().apply {
            putString("vin", car.vin)
        }
        findNavController().navigate(R.id.action_carFragment_to_carDetailsFragment, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
