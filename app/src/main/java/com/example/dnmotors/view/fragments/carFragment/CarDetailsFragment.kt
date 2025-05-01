package com.example.dnmotors.view.fragments.carFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarDetailsBinding
import com.example.dnmotors.model.Car
import com.example.dnmotors.view.adapter.CarImageAdapter
import com.google.firebase.firestore.FirebaseFirestore

class CarDetailsFragment : Fragment() {

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var car: Car

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vinFromArgs = arguments?.getString("vin")
        if (vinFromArgs != null) {
            loadCarByVin(vinFromArgs)
        } else {
            val carFromArgs = arguments?.getParcelable<Car>("car")
            if (carFromArgs != null) {
                car = carFromArgs
                displayCarInfo(car)
            } else {
                Toast.makeText(context, "Car data not found", Toast.LENGTH_SHORT).show()
            }
        }

        if (vinFromArgs != null) {
            loadCarByVin(vinFromArgs)
        }

    }


    private fun loadCarByVin(vin: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Cars")
            .whereEqualTo("vin", vin)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.first()
                    val loadedCar = doc.toObject(Car::class.java)
                    car = loadedCar
                    if (_binding != null) {
                        displayCarInfo(car)
                    }
                } else {
                    Toast.makeText(context, "Car not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading car", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayCarInfo(car: Car) {

        binding.buttonWrite.setOnClickListener {
            car.dealerId?.let { dealerId ->
                val action = CarDetailsFragmentDirections
                    .actionCarDetailsFragmentToMessagesFragment(
                        carId = car.vin,
                        dealerId = dealerId
                    )
                findNavController().navigate(action)
            } ?: Toast.makeText(context, "Invalid car or dealer data", Toast.LENGTH_SHORT).show()
        }

        binding.imageViewShare.setOnClickListener {
            val deepLinkUrl = "darkhan-nursultan-alinur-653a63.gitlab.io/car/${car.vin}"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this car: $deepLinkUrl")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        binding.buttonCall.setOnClickListener {
            car.phoneNumber?.let { phoneNumber ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            }
        }


        val adapter = CarImageAdapter(car.imageUrl)
        binding.viewPagerCarImages.adapter = adapter

        // Подключаем индикатор
        binding.dotsIndicator.attachTo(binding.viewPagerCarImages)

        binding.textViewBrandModel.text = "${car.brand} ${car.model} ${car.year}"
        binding.textViewPrice.text = "${car.price} ₸"
        binding.textViewDescription.text = car.description
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
