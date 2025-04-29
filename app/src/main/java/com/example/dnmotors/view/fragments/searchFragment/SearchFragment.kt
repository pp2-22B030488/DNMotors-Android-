package com.example.dnmotors.view.fragments.searchFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidadvanceddnmotors.ui.dialogs.FilterDialog
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentSearchBinding
import com.example.dnmotors.model.Car
import com.example.dnmotors.view.adapter.CarAdapter
import com.example.dnmotors.view.fragments.favouritesFragment.FavouritesManager
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var carAdapter: CarAdapter
    private var filterDialog: FilterDialog? = null
    private val db = FirebaseFirestore.getInstance()
    private val carList = mutableListOf<Car>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadCars()
        showFilterDialog()
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(
            mutableListOf(),
            onItemClick = { car ->
                val bundle = Bundle().apply {
                    putParcelable("car", car)
                }
                findNavController().navigate(R.id.action_searchFragment_to_carDetailsFragment, bundle)
            }
        )

        binding.rvCars.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carAdapter
        }
    }

    private fun loadCars() {
        db.collection("Cars")
            .get()
            .addOnSuccessListener { result ->
                carList.clear()
                for (document in result) {
                    val car = document.toObject(Car::class.java)
                    Log.d("SearchFragment", "Loaded car: $car") // Логируем полученные машины
                    car.isLiked = FavouritesManager.likedCars.any { it.vin == car.vin }
                    carList.add(car)
                }
                carAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load cars", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFilterDialog() {
        filterDialog = FilterDialog().apply {
            setOnFilterAppliedListener { filter ->
                val filteredCars = filterCars(filter)
                carAdapter.updateList(filteredCars)
            }
        }
        filterDialog?.show(childFragmentManager, "filter_dialog")
    }

    private fun filterCars(filter: FilterDialog.Filter): List<Car> {
        return carList.filter { car ->
            (filter.state == FilterDialog.State.ALL ||
                (filter.state == FilterDialog.State.NEW && car.condition.equals("new", true)) ||
                (filter.state == FilterDialog.State.USED && car.condition.equals("used", true))) &&
            (filter.brandModel.isNullOrBlank() || car.brand.contains(filter.brandModel, true) || car.model.contains(filter.brandModel, true)) &&
            (filter.year == null || car.year == filter.year) &&
            (filter.priceFrom == null || car.price >= filter.priceFrom) &&
            (filter.priceTo == null || car.price <= filter.priceTo)
            // Чекбоксы реализуйте по своим данным, если появятся соответствующие поля
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 