package com.example.dnmotors.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dnmotors.R
import com.example.dnmotors.viewmodel.AuthResult
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.dnmotors.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivitySignInBinding
    private val authViewModel: AuthViewModel by viewModel()

    private val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (authViewModel.isUserSignedIn()) {
            Log.d(TAG, "User already signed in, navigating to Main.")
            navigateToMain()
            return
        }

        setupGoogleSignInLauncher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignInLauncher() {
        googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)

                    if (account?.idToken != null) {
                        Log.d(TAG, "Google Sign-In successful, obtained ID token.")
                        authViewModel.signInWithGoogle(account.idToken!!)
                    } else {
                        Log.e(TAG, "Google Sign-In failed: Account or ID Token is null.")
                        showError("Google Sign-In failed: Could not get token.")
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google Sign-In failed with ApiException: ${e.statusCode}", e)
                    showError("Google Sign-In error: ${e.localizedMessage} (Code: ${e.statusCode})")
                } catch (e: Exception) {
                    Log.e(TAG, "Google Sign-In failed with unexpected exception.", e)
                    showError("An unexpected error occurred during Google Sign-In.")
                }
            } else {
                Log.w(TAG, "Google Sign-In flow cancelled or failed. Result code: ${result.resultCode}")
                // Optional: Show a message if the user cancelled
                // if (result.resultCode == RESULT_CANCELED) {
                //     showError("Google Sign-In cancelled.")
                // }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                showError("Please enter both email and password.")
                return@setOnClickListener
            }
            Log.d(TAG, "Attempting email sign-in for: $email")
            authViewModel.signIn(email, password)
        }

        binding.btnGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.tvSignUp.setOnClickListener {
            Log.d(TAG, "Navigating to RegisterActivity.")
            startActivity(Intent(this, RegisterActivity::class.java))
            // Keep SignInActivity in backstack or finish? Decide based on UX.
            // finish() // Optional: finish SignInActivity
        }
    }

    private fun launchGoogleSignIn() {
        Log.d(TAG, "Launching Google Sign-In intent.")
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            // Ensure user is signed out from Google locally before starting new flow
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up or launching Google Sign-In intent.", e)
            showError("Could not start Google Sign-In. Please try again.")
        }
    }


    private fun observeViewModel() {
        authViewModel.authState.observe(this) { result ->
            setLoading(result is AuthResult.Loading)

            when (result) {
                is AuthResult.Success -> {
                    Log.i(TAG, "Auth successful, navigating to Main.")
                    navigateToMain()
                }
                is AuthResult.Error -> {
                    Log.e(TAG, "Auth failed: ${result.message}")
                    showError(result.message)
                }
                is AuthResult.Loading -> {
                    Log.d(TAG, "Auth process loading...")
                    // Loading state handled by setLoading
                }
                // else -> {} // Handle Idle state if added
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Optionally update a TextView instead of Toast
        // binding.tvError.text = message
        // binding.tvError.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
        // Disable text fields during loading? Optional.
        // binding.etEmail.isEnabled = !isLoading
        // binding.etPassword.isEnabled = !isLoading
    }
}