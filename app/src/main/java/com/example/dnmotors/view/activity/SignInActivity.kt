package com.example.dnmotors.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivitySignInBinding
import com.example.dnmotors.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivitySignInBinding
    private val viewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isUserSignedIn()) {
            Log.d("SignInActivity", "Already logged in. Navigating to MainActivity.")
            navigateToMain()
            return
        }

        // Email Sign-In
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                Log.w("SignInActivity", "Empty email or password")
                return@setOnClickListener
            }

            viewModel.signIn(email, password)
        }

        viewModel.authState.observe(this) { isSuccess ->
            if (isSuccess) {
                Log.d("SignInActivity", "Sign-in successful")
                navigateToMain()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                Log.e("SignInActivity", "Email/Password authentication failed")
            }
        }

        // Google Sign-In setup
        googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                if (account != null && account.idToken != null) {
                    viewModel.signInWithGoogle(account.idToken!!)
                } else {
                    Toast.makeText(this, "Google Sign-In failed: Account or token null", Toast.LENGTH_SHORT).show()
                    Log.e("SignInActivity", "Google Sign-In failed: Account or token was null")
                }

            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("SignInActivity", "Google Sign-In exception: ${e.message}", e)
            } catch (e: Exception) {
                Toast.makeText(this, "Unexpected error during Google Sign-In", Toast.LENGTH_SHORT).show()
                Log.e("SignInActivity", "Unexpected exception during Google Sign-In", e)
            }
        }

        // Google Sign-In button
        binding.btnGoogle.setOnClickListener {
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(this, gso)
                googleLauncher.launch(client.signInIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error launching Google Sign-In", Toast.LENGTH_SHORT).show()
                Log.e("SignInActivity", "Error launching Google Sign-In intent", e)
            }
        }

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
