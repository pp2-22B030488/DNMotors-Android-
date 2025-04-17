package com.example.dnmotors.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dnmotors.R
import com.example.dnmotors.viewmodel.AuthResult
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModel()
    private lateinit var googleRegisterLauncher: ActivityResultLauncher<Intent>
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleRegisterLauncher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleRegisterLauncher() {
        googleRegisterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)

                    if (account?.idToken != null) {
                        Log.d(TAG, "Google Sign-In successful (for registration), obtained ID token.")
                        authViewModel.registerWithGoogle(account.idToken!!)
                    } else {
                        Log.e(TAG, "Google Sign-In (for registration) failed: Account or ID Token is null.")
                        showError("Google Registration failed: Could not get token.")
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google Sign-In (for registration) failed with ApiException: ${e.statusCode}", e)
                    showError("Google Registration error: ${e.localizedMessage} (Code: ${e.statusCode})")
                } catch (e: Exception) {
                    Log.e(TAG, "Google Sign-In (for registration) failed with unexpected exception.", e)
                    showError("An unexpected error occurred during Google Registration.")
                }
            } else {
                Log.w(TAG, "Google Sign-In (for registration) flow cancelled or failed. Result code: ${result.resultCode}")
                // Optional: Show a message if the user cancelled
                // if (result.resultCode == RESULT_CANCELED) { showError("Google Registration cancelled.") }
            }
        }
    }

    private fun setupClickListeners() {
        binding.bRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            // Basic validation
            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                showError("Please fill in all fields.")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Password must be at least 6 characters.")
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting registration for: $email, Name: $name")
            authViewModel.register(email, password, name)
        }
        binding.btnGoogle.setOnClickListener {
            Log.d(TAG, "Google Register button clicked.")
            launchGoogleSignInForRegister()
        }
        binding.tvSignIn.setOnClickListener {
            Log.d(TAG, "Navigating back to SignInActivity.")
            finish()
        }

    }

    private fun launchGoogleSignInForRegister() {
        Log.d(TAG, "Launching Google Sign-In intent for registration.")
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
//                .requestProfile() // DEFAULT_SIGN_IN includes profile
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                googleRegisterLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up or launching Google Sign-In intent for registration.", e)
            showError("Could not start Google Registration. Please try again.")
        }
    }
    private fun observeViewModel() {
        authViewModel.authState.observe(this) { result ->
            setLoading(result is AuthResult.Loading)

            when (result) {
                is AuthResult.Success -> {
                    Log.i(TAG, "Registration successful, navigating to Main.")
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain("CarFragment")
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Registration failed: ${result.message}")
                    showError("Registration failed: ${result.message}")
                }
                is AuthResult.Loading -> {
                    Log.d(TAG, "Registration process loading...")
                }
            }
        }
    }

    private fun navigateToMain(fragmentToOpen: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            fragmentToOpen?.let {
                putExtra("OPEN_FRAGMENT", it)
                Log.d(TAG,"Adding OPEN_FRAGMENT extra: $it")
            }
        }
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // binding.tvErrorRegister.text = message // Example if you add a TextView
        // binding.tvErrorRegister.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
//        binding.progressBarRegister.visibility = if (isLoading) View.VISIBLE else View.GONE // Ensure you have a ProgressBar with this ID
        binding.bRegister.isEnabled = !isLoading
        // binding.etEmail.isEnabled = !isLoading // Optional: disable fields
        // binding.etPassword.isEnabled = !isLoading
        // binding.etName.isEnabled = !isLoading
    }
}