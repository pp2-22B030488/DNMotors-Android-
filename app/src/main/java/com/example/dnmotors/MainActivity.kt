package com.example.dnmotors

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dnmotors.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        setUpActionBar()
        val navController = findNavController(R.id.nav_host_fragment)

        val openFragment = intent.getStringExtra("OPEN_FRAGMENT")
        Log.d("MyLog", "Open fragment: $openFragment")
        Log.d("MyLog", "Received OPEN_FRAGMENT in MainActivity: $openFragment") // Лог для проверки

        if (openFragment == "CarFragment") {
            navController.navigate(R.id.carFragment)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setupWithNavController(navController)

        enableEdgeToEdge()
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }


    private fun setUpActionBar() {
        val ab = supportActionBar
        val photoUrl = auth.currentUser?.photoUrl ?: return

        Picasso.get().load(photoUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val dIcon = BitmapDrawable(resources, bitmap)
                runOnUiThread {
                    ab?.setDisplayHomeAsUpEnabled(true)
                    ab?.setHomeAsUpIndicator(dIcon)
                    ab?.title = auth.currentUser?.displayName
                }
            }

            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: android.graphics.drawable.Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {}
        })
    }

}
