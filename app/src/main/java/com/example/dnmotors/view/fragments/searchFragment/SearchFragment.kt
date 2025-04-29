package com.example.dnmotors.view.fragments.searchFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidadvanceddnmotors.ui.dialogs.FilterDialog
import com.example.dnmotors.databinding.FragmentSearchBinding
import com.example.dnmotors.model.Car
import com.example.dnmotors.view.adapter.CarAdapter

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var carAdapter: CarAdapter
    private var allCars: List<Car> = emptyList()
    private var filterDialog: FilterDialog? = null

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
                // TODO: обработка клика по машине
            }
        )

        binding.rvCars.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = carAdapter
        }
    }

    private fun loadCars() {
        // Пример тестовых данных (замените на загрузку из Firebase)
        allCars = listOf(
            Car(brand = "BMW", model = "X5", year = 2024, price = 50000, condition = "new", imageUrl = listOf("")),
            Car(brand = "Toyota", model = "Camry", year = 2023, price = 30000, condition = "used", imageUrl = listOf("")),
            Car(brand = "Honda", model = "Civic", year = 2022, price = 20000, condition = "used", imageUrl = listOf("")),
            Car(brand = "BMW", model = "3 Series", year = 2021, price = 25000, condition = "used", imageUrl = listOf("")),
            Car(brand = "Audi", model = "A4", year = 2024, price = 40000, condition = "new", imageUrl = listOf(""))
        )
        carAdapter.updateList(allCars)
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
        return allCars.filter { car ->
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