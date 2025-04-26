package com.example.dnmotors.view.fragments.searchFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentSearchBinding
import com.example.dnmotors.model.SearchFilters
import com.example.dnmotors.view.adapter.CarAdapter
import com.example.dnmotors.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var carAdapter: CarAdapter
    private var currentFilters = SearchFilters()

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
        setupSearchBar()
        setupFilterButton()
        setupObservers()
        setupFilterDialog()
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(
            cars = mutableListOf(),
            onItemClick = { car ->
                val bundle = Bundle().apply {
                    putString("carId", car.id)
                }
                findNavController().navigate(R.id.action_searchFragment_to_carDetailsFragment, bundle)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener { text ->
            currentFilters = currentFilters.copy(query = text.toString())
            viewModel.searchCars(currentFilters)
        }
    }

    private fun setupFilterButton() {
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.observe(viewLifecycleOwner) { cars ->
                carAdapter.updateList(cars)
                binding.emptyState.visibility = if (cars.isEmpty()) View.VISIBLE else View.GONE
            }

            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            viewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupFilterDialog() {
        viewModel.availableBrands.observe(viewLifecycleOwner) { brands ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brands)
            binding.brandAutoComplete.setAdapter(adapter)
        }

        viewModel.availableBodyTypes.observe(viewLifecycleOwner) { bodyTypes ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bodyTypes)
            binding.bodyTypeAutoComplete.setAdapter(adapter)
        }

        viewModel.availableFuelTypes.observe(viewLifecycleOwner) { fuelTypes ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fuelTypes)
            binding.fuelTypeAutoComplete.setAdapter(adapter)
        }

        viewModel.availableTransmissionTypes.observe(viewLifecycleOwner) { transmissionTypes ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, transmissionTypes)
            binding.transmissionAutoComplete.setAdapter(adapter)
        }
    }

    private fun showFilterDialog() {
        binding.filterDialog.visibility = View.VISIBLE

        // Setup price range slider
        binding.priceRangeSlider.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                val values = slider.values
                currentFilters = currentFilters.copy(
                    minPrice = values[0].toDouble(),
                    maxPrice = values[1].toDouble()
                )
                updatePriceText()
            }
        }

        // Setup year range slider
        binding.yearRangeSlider.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                val values = slider.values
                currentFilters = currentFilters.copy(
                    minYear = values[0].toInt(),
                    maxYear = values[1].toInt()
                )
                updateYearText()
            }
        }

        // Setup mileage range slider
        binding.mileageRangeSlider.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                val values = slider.values
                currentFilters = currentFilters.copy(
                    minMileage = values[0].toInt(),
                    maxMileage = values[1].toInt()
                )
                updateMileageText()
            }
        }

        // Setup sort options
        binding.sortOptions.setOnCheckedChangeListener { _, checkedId ->
            currentFilters = currentFilters.copy(
                sortBy = when (checkedId) {
                    R.id.sort_newest -> SearchFilters.SortOption.NEWEST
                    R.id.sort_oldest -> SearchFilters.SortOption.OLDEST
                    R.id.sort_price_low -> SearchFilters.SortOption.PRICE_LOW_TO_HIGH
                    R.id.sort_price_high -> SearchFilters.SortOption.PRICE_HIGH_TO_LOW
                    R.id.sort_mileage_low -> SearchFilters.SortOption.MILEAGE_LOW_TO_HIGH
                    R.id.sort_mileage_high -> SearchFilters.SortOption.MILEAGE_HIGH_TO_LOW
                    else -> SearchFilters.SortOption.NEWEST
                }
            )
        }

        // Setup apply button
        binding.applyFiltersButton.setOnClickListener {
            viewModel.searchCars(currentFilters)
            binding.filterDialog.visibility = View.GONE
        }

        // Setup clear button
        binding.clearFiltersButton.setOnClickListener {
            clearFilters()
        }

        // Setup close button
        binding.closeFilterDialog.setOnClickListener {
            binding.filterDialog.visibility = View.GONE
        }
    }

    private fun updatePriceText() {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        binding.priceRangeText.text = "${format.format(currentFilters.minPrice)} - ${format.format(currentFilters.maxPrice)}"
    }

    private fun updateYearText() {
        binding.yearRangeText.text = "${currentFilters.minYear} - ${currentFilters.maxYear}"
    }

    private fun updateMileageText() {
        binding.mileageRangeText.text = "${currentFilters.minMileage} - ${currentFilters.maxMileage} km"
    }

    private fun clearFilters() {
        currentFilters = SearchFilters()
        binding.priceRangeSlider.values = listOf(0f, 100000f)
        binding.yearRangeSlider.values = listOf(1900f, 2024f)
        binding.mileageRangeSlider.values = listOf(0f, 300000f)
        binding.sortOptions.check(R.id.sort_newest)
        binding.brandAutoComplete.text.clear()
        binding.bodyTypeAutoComplete.text.clear()
        binding.fuelTypeAutoComplete.text.clear()
        binding.transmissionAutoComplete.text.clear()
        updatePriceText()
        updateYearText()
        updateMileageText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
