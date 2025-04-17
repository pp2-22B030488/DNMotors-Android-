package com.example.dnmotors.view.fragments.favouritesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dnmotors.R
import com.example.dnmotors.view.fragments.carFragment.Car
import com.example.dnmotors.view.fragments.carFragment.CarAdapter

class FavouritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favouritesAdapter: CarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)
        recyclerView = view.findViewById(R.id.recyclerFavourites)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        favouritesAdapter = CarAdapter(
            cars = FavouritesManager.likedCars,
            onItemClick = { /* опционально */ },
            isInFavourites = true
        )
        recyclerView.adapter = favouritesAdapter

        return view
    }
}
