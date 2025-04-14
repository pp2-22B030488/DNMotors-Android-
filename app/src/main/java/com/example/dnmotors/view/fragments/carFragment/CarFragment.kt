package com.example.dnmotors.view.fragments.carFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        adapter = CarAdapter(carList) { car -> showCarDetails(car) }
        recyclerView.adapter = adapter

        fetchCarsFromFirestore()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val logo = view.findViewById<ImageView>(R.id.logo)
        val btnCredit = view.findViewById<ImageView>(R.id.btnCredit)

        btnCredit.setOnClickListener {
            // Переход на другой фрагмент
            findNavController().navigate(R.id.action_carFragment_to_carCreditFragment)
        }
    }


    private fun fetchCarsFromFirestore() {
        db.collection("Cars")
            .get()
            .addOnSuccessListener { result ->
                carList.clear()
                for (document in result) {
                    val car = document.toObject(Car::class.java)
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
            putParcelable("car", car)
        }
        findNavController().navigate(R.id.action_carFragment_to_carDetailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}