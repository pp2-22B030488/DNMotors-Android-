package com.example.dnmotors.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityRegisterBinding
import com.example.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RegisterActivity : AppCompatActivity() {
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>

    private val authRepository: AuthRepository by inject()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, name)
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
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
    }

    private fun registerUser(email: String, password: String, name: String) {
        lifecycleScope.launch {
            val result = authRepository.registerWithEmail(email, password, name)
            result.onSuccess {
                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure {
                Log.e("RegisterActivity", "Registration failed", it)
                Toast.makeText(this@RegisterActivity, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_FRAGMENT", "CarFragment")
        startActivity(intent)
        finish()
    }
}
