package com.example.dnmotors.view.fragments.carFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarDetailsBinding
import com.example.dnmotors.view.adapter.CarDetailsAdapter
import com.example.domain.model.Car
import com.example.dnmotors.view.adapter.CarImageAdapter
import com.google.firebase.firestore.FirebaseFirestore

class CarDetailsFragment : Fragment() {

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var car: Car
    private var autoScrollHandler: Handler? = null
    private var autoScrollRunnable: Runnable? = null
    private var isAutoScrolling = false


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
    private fun extractYoutubeId(url: String): String {
        val regex = "(?<=v=|/)([a-zA-Z0-9_-]{11})(?=&|\\?|$)".toRegex()
        return regex.find(url)?.value ?: ""
    }

    private fun startAutoScroll(itemCount: Int, itemDelay: Long) {
        autoScrollHandler = Handler(Looper.getMainLooper())
        autoScrollRunnable = object : Runnable {
            override fun run() {
                val currentItem = binding.viewPager360.currentItem
                val nextItem = if (currentItem == itemCount - 1) 0 else currentItem + 1
                binding.viewPager360.setCurrentItem(nextItem, false)
                autoScrollHandler?.postDelayed(this, itemDelay) // Каждые 500 мс (0.5 сек)
            }
        }
        autoScrollHandler?.postDelayed(autoScrollRunnable!!, itemDelay)
        isAutoScrolling = true
    }

    private fun stopAutoScroll() {
        autoScrollHandler?.removeCallbacks(autoScrollRunnable!!)
        isAutoScrolling = false
    }
    private fun pauseAutoScrollAndResume(delayMillis: Long, itemCount: Int, itemDelay: Long) {
        stopAutoScroll()
        Handler(Looper.getMainLooper()).postDelayed({
            startAutoScroll(itemCount, itemDelay)
        }, delayMillis)
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

        if (!car.image360Url.isNullOrEmpty()) {
            val adapter360 = CarDetailsAdapter(car.image360Url)
            binding.viewPager360.adapter = adapter360

            val itemCount = car.image360Url.size
            var itemDelay = 1000

            // Отключаем анимацию перелистывания при свайпе пальцем
            binding.viewPager360.setPageTransformer { page, position ->
                page.translationX = -position * page.width
                page.alpha = if (position == 0f) 1f else 0f
            }

            binding.buttonNext.setOnClickListener {
//                stopAutoScroll()
                val currentItem = binding.viewPager360.currentItem
                val nextItem = if (currentItem == itemCount - 1) 0 else currentItem + 1
                binding.viewPager360.setCurrentItem(nextItem, false)
//                pauseAutoScrollAndResume(5000, itemCount, itemDelay.toLong()) // 5 сек пауза

            }

            binding.buttonPrev.setOnClickListener {
//                stopAutoScroll()
                val currentItem = binding.viewPager360.currentItem
                val prevItem = if (currentItem == 0) itemCount - 1 else currentItem - 1
                binding.viewPager360.setCurrentItem(prevItem, false)
//                pauseAutoScrollAndResume(5000, itemCount, itemDelay.toLong()) // 5 сек пауза

            }
//            startAutoScroll(itemCount, itemDelay.toLong())

        } else {
            Toast.makeText(context, "360 images not available", Toast.LENGTH_SHORT).show()
        }



        binding.textViewBrandModel.text = "${car.brand} ${car.model} ${car.year}"
        binding.textViewPrice.text = "${car.price} ₸"
        binding.textViewGeneration.text = car.generation
        binding.textViewEngineVolumeSpec.text = car.engineCapacity
        binding.textViewFuelTypeSpec.text = car.fuelType
        binding.textViewVIN.text = car.vin
        binding.textViewTransmissionSpec.text = car.transmission
        binding.textViewDriveType.text = car.driveType
        binding.textViewBodyType.text = car.bodyType
        binding.textViewMileage.text = car.mileageKm.toString()
        binding.textViewLocation.text = car.location
        binding.textViewCondition.text = car.condition


        binding.textViewDescription.text = car.description


        // Видео тест-драйва
        if (!car.testDriveUrl.isNullOrEmpty()) {
            binding.webViewTestDrive.visibility = View.VISIBLE
            val videoId = extractYoutubeId(car.testDriveUrl)

            val html = """
        <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId" 
        frameborder="0" allowfullscreen></iframe>
    """

            binding.webViewTestDrive.settings.javaScriptEnabled = true
            binding.webViewTestDrive.loadData(html, "text/html", "utf-8")
        } else {
            binding.webViewTestDrive.visibility = View.GONE
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
