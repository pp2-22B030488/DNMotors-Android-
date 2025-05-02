package com.example.data.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.domain.model.CarEntity

@Dao
interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAllCars(): LiveData<List<CarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<CarEntity>)

    @Query("DELETE FROM cars")
    suspend fun clearCars()

    @Query("SELECT * FROM cars WHERE vin = :vin LIMIT 1")
    suspend fun getCarByVin(vin: String): CarEntity?
}
