package com.example.dnmotors.view.fragments.comparisionFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.databinding.FragmentCarComparisonBinding
import com.example.dnmotors.view.fragments.carFragment.Car
import com.google.firebase.firestore.FirebaseFirestore

class CarComparisonFragment : Fragment() {
    private var _binding: FragmentCarComparisonBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var car1: Car? = null
    private var car2: Car? = null
    private lateinit var comparisonAdapter: ComparisonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        comparisonAdapter = ComparisonAdapter()
        binding.rvComparison.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = comparisonAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectCar1.setOnClickListener {
            CarSelectionBottomSheet { selectedCar ->
                car1 = selectedCar
                binding.tvCar1Name.text = selectedCar.model
                updateComparison()
            }.show(childFragmentManager, "Car1Selector")
        }

        binding.btnSelectCar2.setOnClickListener {
            CarSelectionBottomSheet { selectedCar ->
                car2 = selectedCar
                binding.tvCar2Name.text = selectedCar.model
                updateComparison()
            }.show(childFragmentManager, "Car2Selector")
        }
    }


    private fun updateComparison() {
        if (car1 != null && car2 != null) {
            val comparisonItems = listOf(
                ComparisonItem("Model", car1!!.model, car2!!.model),
                ComparisonItem("Year", car1!!.year.toString(), car2!!.year.toString()),
                ComparisonItem("Price", "${car1!!.price}$", "${car2!!.price}$"),
                ComparisonItem("Mileage", "${car1!!.mileageKm} km", "${car2!!.mileageKm} km"),
                ComparisonItem("Drive Type", car1!!.driveType, car2!!.driveType),
                ComparisonItem("Engine Volume", car1!!.fuelType, car2!!.fuelType)
            )
            comparisonAdapter.submitList(comparisonItems)
        }
    }

    data class ComparisonItem(
        val parameterName: String,
        val car1Value: String,
        val car2Value: String
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 