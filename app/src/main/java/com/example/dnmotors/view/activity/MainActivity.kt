package com.example.dnmotors.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if (auth.currentUser == null) {
            Log.w(TAG, "No authenticated user found, redirecting to SignInActivity.")
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        Log.d(TAG, "User authenticated: ${auth.currentUser?.uid}. Setting up UI.")

        setupFirestorePersistence()
        setupNavigation()
        setupActionBar()
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
        val navController = findNavController(R.id.nav_host_fragment)
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
                            runOnUiThread {
                                setDefaultActionBarIcon(ab)
                            }
                        }

                        override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                            Log.d(TAG,"Preparing to load profile picture...")
                        }
                    })
            } else {
                Log.w(TAG, "User photo URL is null. Setting default icon.")
                setDefaultActionBarIcon(ab)
            }
        } ?: Log.w(TAG, "SupportActionBar is null, cannot configure.")
    }

    private fun setDefaultActionBarIcon(ab: androidx.appcompat.app.ActionBar) {
        // Set a default placeholder icon (e.g., a user silhouette)
        // Make sure you have a drawable resource for this
        // ab.setDisplayHomeAsUpEnabled(true)
        // ab.setHomeAsUpIndicator(R.drawable.ic_default_profile) // Replace with your default icon
        // Or just hide the indicator if no icon is desired
        ab.setDisplayHomeAsUpEnabled(false)
    }
}