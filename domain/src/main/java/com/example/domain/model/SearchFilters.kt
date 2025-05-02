package com.example.domain.model

data class SearchFilters(
    val query: String = "",
    val brands: List<String> = emptyList(),
    val bodyTypes: List<String> = emptyList(),
    val fuelTypes: List<String> = emptyList(),
    val transmissionTypes: List<String> = emptyList(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minMileage: Int? = null,
    val maxMileage: Int? = null,
    val sortBy: SortOption = SortOption.NEWEST
) {
    enum class SortOption {
        NEWEST,
        OLDEST,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        MILEAGE_LOW_TO_HIGH,
        MILEAGE_HIGH_TO_LOW
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "query" to query,
            "minPrice" to (minPrice ?: 0.0),
            "maxPrice" to (maxPrice ?: Double.MAX_VALUE),
            "minYear" to (minYear ?: 1900),
            "maxYear" to (maxYear ?: 2100),
            "brands" to brands,
            "bodyTypes" to bodyTypes,
            "fuelTypes" to fuelTypes,
            "transmissionTypes" to transmissionTypes,
            "minMileage" to (minMileage ?: 0),
            "maxMileage" to (maxMileage ?: Int.MAX_VALUE),
            "sortBy" to sortBy.name
        )
    }
} 