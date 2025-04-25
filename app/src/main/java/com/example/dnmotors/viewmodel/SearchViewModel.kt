package com.example.dnmotors.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.model.SearchFilters
import com.example.dnmotors.view.fragments.carFragment.Car
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "SearchViewModel"

    private val _searchResults = MutableLiveData<List<Car>>()
    val searchResults: LiveData<List<Car>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> = _error

    private val _availableBrands = MutableLiveData<List<String>>()
    val availableBrands: LiveData<List<String>> = _availableBrands

    private val _availableBodyTypes = MutableLiveData<List<String>>()
    val availableBodyTypes: LiveData<List<String>> = _availableBodyTypes

    private val _availableFuelTypes = MutableLiveData<List<String>>()
    val availableFuelTypes: LiveData<List<String>> = _availableFuelTypes

    private val _availableTransmissionTypes = MutableLiveData<List<String>>()
    val availableTransmissionTypes: LiveData<List<String>> = _availableTransmissionTypes

    init {
        loadAvailableFilters()
    }

    private fun loadAvailableFilters() {
        viewModelScope.launch {
            try {
                val cars = firestore.collection("cars").get().await()
                
                val brands = mutableSetOf<String>()
                val bodyTypes = mutableSetOf<String>()
                val fuelTypes = mutableSetOf<String>()
                val transmissionTypes = mutableSetOf<String>()

                cars.documents.forEach { doc ->
                    doc.getString("brand")?.let { brands.add(it) }
                    doc.getString("bodyType")?.let { bodyTypes.add(it) }
                    doc.getString("fuelType")?.let { fuelTypes.add(it) }
                    doc.getString("transmission")?.let { transmissionTypes.add(it) }
                }

                _availableBrands.value = brands.sorted()
                _availableBodyTypes.value = bodyTypes.sorted()
                _availableFuelTypes.value = fuelTypes.sorted()
                _availableTransmissionTypes.value = transmissionTypes.sorted()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading available filters", e)
                _error.value = "Failed to load filter options"
            }
        }
    }

    fun searchCars(filters: SearchFilters) {
        _isLoading.value = true
        
        var query: Query = firestore.collection("cars")
        
        // Apply filters
        if (filters.query.isNotEmpty()) {
            query = query.whereGreaterThanOrEqualTo("name", filters.query)
                .whereLessThanOrEqualTo("name", filters.query + "\uf8ff")
        }
        
        if (filters.brands.isNotEmpty()) {
            query = query.whereIn("brand", filters.brands)
        }
        
        if (filters.bodyTypes.isNotEmpty()) {
            query = query.whereIn("bodyType", filters.bodyTypes)
        }
        
        if (filters.fuelTypes.isNotEmpty()) {
            query = query.whereIn("fuelType", filters.fuelTypes)
        }
        
        if (filters.transmissionTypes.isNotEmpty()) {
            query = query.whereIn("transmission", filters.transmissionTypes)
        }
        
        if (filters.minPrice != null) {
            query = query.whereGreaterThanOrEqualTo("price", filters.minPrice)
        }
        
        if (filters.maxPrice != null) {
            query = query.whereLessThanOrEqualTo("price", filters.maxPrice)
        }
        
        if (filters.minYear != null) {
            query = query.whereGreaterThanOrEqualTo("year", filters.minYear)
        }
        
        if (filters.maxYear != null) {
            query = query.whereLessThanOrEqualTo("year", filters.maxYear)
        }
        
        if (filters.minMileage != null) {
            query = query.whereGreaterThanOrEqualTo("mileage", filters.minMileage)
        }
        
        if (filters.maxMileage != null) {
            query = query.whereLessThanOrEqualTo("mileage", filters.maxMileage)
        }
        
        // Apply sorting
        query = when (filters.sortBy) {
            SearchFilters.SortOption.NEWEST -> query.orderBy("year", Query.Direction.DESCENDING)
            SearchFilters.SortOption.OLDEST -> query.orderBy("year", Query.Direction.ASCENDING)
            SearchFilters.SortOption.PRICE_LOW_TO_HIGH -> query.orderBy("price", Query.Direction.ASCENDING)
            SearchFilters.SortOption.PRICE_HIGH_TO_LOW -> query.orderBy("price", Query.Direction.DESCENDING)
            SearchFilters.SortOption.MILEAGE_LOW_TO_HIGH -> query.orderBy("mileage", Query.Direction.ASCENDING)
            SearchFilters.SortOption.MILEAGE_HIGH_TO_LOW -> query.orderBy("mileage", Query.Direction.DESCENDING)
        }
        
        query.get()
            .addOnSuccessListener { documents ->
                val cars = documents.mapNotNull { document ->
                    try {
                        document.toObject(Car::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                _searchResults.value = cars
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _searchResults.value = emptyList()
                _isLoading.value = false
            }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _error.value = null
    }
} 