package com.example.dnmotors.view.fragments.authFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.ActivityRegisterBinding
import com.example.dnmotors.viewmodel.AuthResult
import com.example.dnmotors.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModel()
    private lateinit var googleRegisterLauncher: ActivityResultLauncher<Intent>
    private val TAG = "RegisterFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGoogleRegisterLauncher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleRegisterLauncher() {
        googleRegisterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
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
                if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
                    showError("Google Registration cancelled.")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.bRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            if (email.isBlank() || password.isBlank() || name.isBlank() || location.isBlank() || phoneNumber.isBlank()) {
                showError("Please fill in all fields.")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Password must be at least 6 characters.")
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting registration for: $email, Name: $name")
            authViewModel.register(email, password, name, location, phoneNumber)
        }

        binding.btnGoogle.setOnClickListener {
            Log.d(TAG, "Google Register button clicked.")
            launchGoogleSignInForRegister()
        }

        binding.tvSignIn.setOnClickListener {
            Log.d(TAG, "Navigating back to SignInFragment.")
            findNavController().navigateUp()
        }
    }

    private fun launchGoogleSignInForRegister() {
        Log.d(TAG, "Launching Google Sign-In intent for registration.")
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

            googleSignInClient.signOut().addOnCompleteListener {
                Log.d(TAG, "Successfully signed out previous session.")
                googleRegisterLauncher.launch(googleSignInClient.signInIntent)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Sign out failed.", e)
                showError("Google Sign-Out failed, please try again.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up or launching Google Sign-In intent for registration.", e)
            showError("Could not start Google Registration. Please try again.")
        }
    }

    private fun observeViewModel() {
        authViewModel.authState.observe(viewLifecycleOwner) { result ->
            setLoading(result is AuthResult.Loading)

            when (result) {
                is AuthResult.Success -> {
                    Log.i(TAG, "Registration successful, navigating to Main.")
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
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

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.bRegister.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 