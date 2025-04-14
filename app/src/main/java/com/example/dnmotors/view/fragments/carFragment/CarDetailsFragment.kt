package com.example.dnmotors.view.fragments.carFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dnmotors.databinding.FragmentCarDetailsBinding
import com.example.dnmotors.view.fragments.carFragment.Car

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

        car = arguments?.getParcelable("car")!!

        // Загрузка изображения
        Glide.with(requireContext())
            .load(car.imageUrl)
            .placeholder(com.example.dnmotors.R.drawable.tayota_camry_xv80)
            .into(binding.imageViewCar)

        binding.textViewBrandModel.text = "${car.brand}" + " " + "${car.model}" + " " + "${car.year}"
//        binding.textViewModel.text = car.model
        binding.textViewPrice.text = "${car.price} ₸"
//        binding.textViewYear.text = "Year: ${car.year}"
//        binding.textViewMileage.text = "Mileage: ${car.mileageKm} km"
//        binding.textViewTransmission.text = "Transmission: ${car.transmission}"
//        binding.textViewDriveType.text = "Drive Type: ${car.driveType}"
//        binding.textViewLocation.text = "Location: ${car.location}"
        binding.textViewDescription.text = car.description
//        binding.textViewCondition.text = "Condition: ${car.condition}"
//        binding.textViewBodyType.text = "Body Type: ${car.bodyType}"
//        binding.textViewEngineVolume.text = "Engine Volume: ${car.engineVolume}"
//        binding.textViewHorsepower.text = "Horsepower: ${car.horsepower}"
//        binding.textViewSteeringWheel.text = "Steering Wheel: ${car.steeringWheel}"
//        binding.textViewOwnersCount.text = "Owners count: ${car.ownersCount}"
//        binding.textViewVIN.text = "VIN: ${car.vin}"
//        binding.textViewExteriorFeatures.text = "Exterior Features: ${car.exteriorFeatures.joinToString()}"
//        binding.textViewInteriorFeatures.text = "Interior Features: ${car.interiorFeatures.joinToString()}"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
