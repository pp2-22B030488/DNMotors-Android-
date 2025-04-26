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
            car = arguments?.getParcelable("car")!!
            displayCarInfo(car)
        }
        binding.buttonWrite.setOnClickListener {
            car?.let {
                val action = it.dealerId?.let { it1 ->
                    CarDetailsFragmentDirections
                        .actionCarDetailsFragmentToMessagesFragment(
                            carId = it.vin,
                            dealerId = it1
                        )
                }
                if (action != null) {
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "Invalid car or dealer data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.imageViewShare.setOnClickListener {
            val deepLinkUrl = "https://dnmotors.com/car/${car.vin}"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this car: $deepLinkUrl")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }



        binding.buttonCall.setOnClickListener {
            val phoneNumber = car.phoneNumber
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }

    private fun loadCarByVin(vin: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("cars")
            .whereEqualTo("vin", vin)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.first()
                    car = doc.toObject(Car::class.java)
                    displayCarInfo(car)
                } else {
                    // Handle car not found
                }
            }
            .addOnFailureListener {
                // Handle error
            }
    }
    private fun displayCarInfo(car: Car) {
        Glide.with(requireContext())
            .load(car.imageUrl.firstOrNull())
            .placeholder(R.drawable.tayota_camry_xv80)
            .into(binding.imageViewCar)

        binding.textViewBrandModel.text = "${car.brand} ${car.model} ${car.year}"
        binding.textViewPrice.text = "${car.price} ₸"
        binding.textViewDescription.text = car.description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
