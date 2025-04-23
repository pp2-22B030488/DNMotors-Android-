package com.example.dnmotors.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityMainBinding
import com.example.dnmotors.view.fragments.authFragment.SignInFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class MainActivity : AppCompatActivity(), SignInFragment.LoginListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            setupFirestorePersistence()
            setupNavigation()
            setupActionBar()
            handleDeepLink(intent?.data)
        } else {
            navigateToLoginScreen()
        }

        setupFirestorePersistence()
        setupNavigation()
        setupActionBar()
        handleDeepLink(intent?.data)
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, SignInFragment::class.java)
        startActivity(intent)
        finish() // Завершаем текущую активность, чтобы предотвратить возврат на неё
    }

    private fun handleDeepLink(uri: Uri?) {
        uri?.let {
            if (it.pathSegments.firstOrNull() == "car") {
                val vin = it.lastPathSegment
                vin?.let {
                    val bundle = Bundle().apply {
                        putString("vin", vin)
                    }

                    val navHostFragment = supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController

                    navController.navigate(R.id.carDetailsFragment, bundle)
                }
            }
        }
    }

    private fun setupFirestorePersistence() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            Log.i(TAG, "Firestore persistence enabled.")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling Firestore persistence.", e)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        intent.getStringExtra("OPEN_FRAGMENT")?.let { fragmentName ->
            Log.d(TAG, "Received OPEN_FRAGMENT extra: $fragmentName")
            if (fragmentName == "CarFragment") {
                try {
                    navController.navigate(R.id.carFragment)
                    Log.d(TAG,"Navigated to CarFragment via intent extra.")
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Failed to navigate to CarFragment: Destination ID not found or invalid.", e)
                }
            }
            intent.removeExtra("OPEN_FRAGMENT")
        }
    }

    private fun setupActionBar() {
        supportActionBar?.let { ab ->
            val currentUser = auth.currentUser
            val photoUrl = currentUser?.photoUrl
            val displayName = currentUser?.displayName ?: "User"

            ab.title = displayName
            Log.d(TAG, "Setting ActionBar title: $displayName")

            if (photoUrl != null) {
                Log.d(TAG, "Loading profile picture from URL: $photoUrl")
                val placeholder = R.drawable.ic_launcher_background
                val errorDrawable = R.drawable.ic_launcher_foreground

                Picasso.get()
                    .load(photoUrl)
                    .placeholder(placeholder)
                    .error(errorDrawable)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            if (bitmap != null) {
                                Log.d(TAG, "Profile picture loaded successfully.")
                                val dIcon = BitmapDrawable(resources, bitmap)
                                runOnUiThread {
                                    ab.setDisplayHomeAsUpEnabled(true)
                                    ab.setHomeAsUpIndicator(dIcon)
                                }
                            } else {
                                Log.w(TAG, "Bitmap loaded as null.")
                                setDefaultActionBarIcon(ab)
                            }
                        }

                        override fun onBitmapFailed(e: Exception?, errorDrawableDrawable: android.graphics.drawable.Drawable?) {
                            Log.e(TAG, "Failed to load profile picture.", e)
                            setDefaultActionBarIcon(ab)
                        }

                        override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                            // Optional: Show placeholder while loading
                        }
                    })
            } else {
                setDefaultActionBarIcon(ab)
            }
        }
    }

    private fun setDefaultActionBarIcon(ab: androidx.appcompat.app.ActionBar) {
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground)
    }

    override fun onLoginSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_signInFragment_to_mainFragment)
        binding.bottomNavigationView.visibility = View.VISIBLE
    }
}