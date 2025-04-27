package com.example.dnmotors.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dnmotors.App.Companion.context
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityMainBinding
import com.example.dnmotors.utils.MessageNotificationUtil
import com.example.dnmotors.view.fragments.authFragment.SignInFragment
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class MainActivity : AppCompatActivity(), SignInFragment.LoginListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var authViewModel: AuthViewModel
    private lateinit var chatViewModel: ChatViewModel
    private val TAG = "MainActivity"
    private var currentChatId: String? = null
    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val currentUser = auth.currentUser
        if (currentUser != null) {
            setupFirestorePersistence()
            setupNavigation()
            setupActionBar()
            fetchAndNavigateUserRole()
            handleDeepLink(intent?.data)

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

                }

            }
        }
    }

    private fun setupChatListeners() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        chatViewModel.chatItems.observe(this) { chats ->
            chats.forEach { chat ->
                chatViewModel.observeMessages("${chat.dealerId}_${chat.userId}", this)
            }
        }

        messageListener = FirebaseFirestore.getInstance()
            .collection("chats")
            .whereEqualTo("dealerId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Message listener error", error)
                    return@addSnapshotListener
                }

                for (doc in snapshot.documents) {
                    val message = doc.toObject(Message::class.java) ?: continue
                    val messageChatId = doc.reference.parent.parent?.id ?: continue

                    if (message.senderId == userId ||
                        message.notificationSent == true ||
                        messageChatId == currentChatId) {
                        continue
                    }

                    MessageNotificationUtil.sendNotification(this, message)
                    doc.reference.update("notificationSent", true)
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update notification status", e)
                        }
                }
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