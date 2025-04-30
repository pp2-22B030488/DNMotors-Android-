package com.example.dnmotors.view.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.example.dnmotors.services.MessageWorkScheduler
import com.example.dnmotors.view.fragments.authFragment.SignInFragment
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : AppCompatActivity(), SignInFragment.LoginListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val TAG = "MainActivity"
    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            setupFirestorePersistence()

            fetchAndNavigateUserRole()

        } else {
            navigateToLoginScreen()
        }
    }
    private fun fetchAndNavigateUserRole() {
        authViewModel.fetchUserRole { role ->
            val cleanRole = role.trim().lowercase()
            Log.d(TAG, "Fetched user role: '$cleanRole'")

            when (cleanRole) {
                "dealer" -> {
                    Log.i(TAG, "Navigating to DealerActivity for dealer user.")
                    val intent = Intent(this, DealerActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Log.i(TAG, "Navigating to main fragment for non-dealer user.")
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.navigate(R.id.carFragment)
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    setupChatListeners()
                    setupNavigation()
                    setupActionBar()
                    handleDeepLinkAndNotification(intent)

                }

            }
        }
    }

    private fun setupChatListeners() {
        chatViewModel.loadChatList(false)

        chatViewModel.chatItems.observeForever { chats ->
            chats.forEach { chat ->
                chatViewModel.observeNewMessages(
                    chatId = "${chat.dealerId}_${chat.userId}",
                    this,
                    activityClass = MainActivity::class.java)
            }
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            MessageWorkScheduler.scheduleWorker(this)
            MessageWorkScheduler.triggerNow(this)
        }
    }

    private fun handleDeepLinkAndNotification(intent: Intent) {
        handleDeepLink(intent.data)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val carId = intent.getStringExtra("carId")
        val dealerId = intent.getStringExtra("dealerId")

        if (!carId.isNullOrEmpty() && !dealerId.isNullOrEmpty()) {
            navigateToChatFragment(carId, dealerId)
            intent.removeExtra("carId")
            intent.removeExtra("dealerId")
        }
    }

    private fun navigateToChatFragment(carId: String, dealerId: String) {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val bundle = Bundle().apply {
                putString("carId", carId)
                putString("dealerId", dealerId)
            }

            Log.d(TAG, "Navigating to messagesFragment with carId: $carId, dealerId: $dealerId")
            if (navController.currentDestination?.id != R.id.messagesFragment) {
                navController.navigate(R.id.messagesFragment, bundle)
            } else {
                Log.d(TAG, "Already on messagesFragment. Bundle: $bundle")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to messagesFragment", e)
        }
    }

    private fun navigateToLoginScreen() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.signInFragment)
        binding.bottomNavigationView.visibility = View.GONE
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
                        }
                    })
            } else {
                setDefaultActionBarIcon(ab)
            }
        }
    }
    override fun attachBaseContext(newBase: Context) {
        val langCode = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("lang", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }


    private fun setDefaultActionBarIcon(ab: androidx.appcompat.app.ActionBar) {
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground)
    }

    override fun onLoginSuccess() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id == R.id.messagesFragment) {
            Log.d(TAG, "Already on messagesFragment. No need to navigate again from onLoginSuccess.")
            binding.bottomNavigationView.visibility = View.VISIBLE
        } else if (navController.currentDestination?.id != R.id.carFragment) {
            Log.d(TAG, "Navigating from ${navController.currentDestination?.label} to carFragment after login.")
            navController.navigate(R.id.action_signInFragment_to_carFragment)
            binding.bottomNavigationView.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "Already on carFragment. Skipping navigation from onLoginSuccess.")
            binding.bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkAndNotification(intent)
    }
}