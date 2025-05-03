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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityMainBinding
import com.example.dnmotors.services.MessageWorkScheduler
import com.example.dnmotors.view.fragments.authFragment.SignInFragment
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.AuthUser
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : AppCompatActivity(), SignInFragment.LoginListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: AuthUser
    private val authViewModel: AuthViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            auth = authViewModel.returnAuth()
        }
        authViewModel.fetchAuthInfo { authUser ->
            setupActionBar(authUser)
        }
        handleDeepLinkAndNotification(intent)

        val currentUser = auth.uid
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
                    if (navController.currentDestination == null || navController.currentDestination?.id == R.id.signInFragment) {
                        navController.navigate(R.id.carFragment)
                    }
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    setupChatListeners()
                    setupNavigation()
                    setupActionBar()
                }

            }
        }
    }

    private fun setupChatListeners() {
        chatViewModel.loadChatList(false)

        chatViewModel.chatItems.observeForever { chats ->
            val safeChats = chats.toList()
            safeChats.forEach { chat ->
                chatViewModel.observeMessages(
                    chatId = "${chat.dealerId}_${chat.userId}",
                    this,
                )
            }
            safeChats.forEach { chat ->
                chatViewModel.observeNewMessages(
                    chatId = "${chat.dealerId}_${chat.userId}",
                    this,
                    activityClass = MainActivity::class.java
                )
            }
        }


        if (auth.uid != null) {
            MessageWorkScheduler.scheduleWorker(this)
            MessageWorkScheduler.triggerNow(this)
        }
    }

    private fun handleDeepLinkAndNotification(intent: Intent) {
        if (intent.data != null || intent.extras != null) {
            Log.d(TAG, "Attempting to handle intent: ${intent.action}, data: ${intent.data}, extras: ${intent.extras}")
            handleDeepLink(intent.data)
            if (intent.data == null || intent.data?.pathSegments?.size ?: 0 < 3 || intent.data?.pathSegments?.getOrNull(intent.data?.pathSegments?.size!! - 2) != "car") {
                handleNotificationIntent(intent)
            } else {
                Log.d(TAG, "Deep link handled, skipping notification intent check.")
            }
        } else {
            Log.d(TAG, "Intent has no data or extras. Not a deep link or notification intent.")
        }
    }

    private fun handleNotificationIntent(intent: Intent) {
        val carId = intent.getStringExtra("carId")
        val dealerId = intent.getStringExtra("dealerId")

        if (!carId.isNullOrEmpty() && !dealerId.isNullOrEmpty()) {
            Log.d(TAG, "Notification intent received with carId: $carId, dealerId: $dealerId")
            navigateToChatFragment(carId, dealerId)
            intent.removeExtra("carId")
            intent.removeExtra("dealerId")
        } else {
            Log.d(TAG, "No valid notification extras found in intent.")
        }
    }
    private fun handleDeepLink(uri: Uri?) {
        if (uri == null) {
            Log.d(TAG, "handleDeepLink called with null URI.")
            return
        }

        Log.d(TAG, "Deep link URI received: $uri")

        clearChatListeners()

        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2 && pathSegments[0] == "car") {
            val vin = pathSegments[1]
            Log.d(TAG, "Extracted VIN: $vin")

            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

            val bundle = Bundle().apply {
                putString("vin", vin)
            }

            navController.navigate(R.id.carFragment)

            navController.navigate(R.id.carDetailsFragment, bundle)

            Log.d(TAG, "Manual navigation to carDetailsFragment with VIN: $vin")
        } else {
            Log.d(TAG, "Unexpected deep link format or missing VIN.")
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

    private fun setupFirestorePersistence() {
        try {
            authViewModel.setupFirestorePersistence()
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

    private fun setupActionBar(authUser: AuthUser? = null) {
        supportActionBar?.let { ab ->
            val currentUser = authUser ?: return@let
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
    private fun clearChatListeners() {
        chatViewModel.chatItems.removeObservers(this)
        authViewModel.clearChatListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkAndNotification(intent)
    }
}