package com.example.dnmotors.view.fragments.authFragment

import android.content.Context
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
import com.example.dnmotors.databinding.ActivitySignInBinding
import com.example.dnmotors.viewdealer.activity.DealerActivity
import com.example.dnmotors.viewmodel.AuthResult
import com.example.dnmotors.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : Fragment() {
    private var _binding: ActivitySignInBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModel()
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>
    private val TAG = "SignInFragment"
    private var listener: LoginListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface LoginListener {
        fun onLoginSuccess()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement LoginListener")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (authViewModel.isUserSignedIn()) {
            onLoginSuccess()
        }

        setupGoogleSignInLauncher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignInLauncher() {
        googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
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
            Log.d(TAG, "Navigating to RegisterFragment.")
            findNavController().navigate(R.id.action_signInFragment_to_registerFragment)
        }
    }

    private fun launchGoogleSignIn() {
        Log.d(TAG, "Launching Google Sign-In intent.")
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up or launching Google Sign-In intent.", e)
            showError("Could not start Google Sign-In. Please try again.")
        }
    }

    private fun observeViewModel() {
        authViewModel.authState.observe(viewLifecycleOwner) { result ->
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
                }
            }
        }
    }

    private fun navigateToMain() {
        authViewModel.fetchUserRole { role ->
            val cleanRole = role.trim().lowercase()
            Log.d(TAG, "Fetched user role: '$cleanRole'")

            if (cleanRole == "dealer") {
                Log.i(TAG, "Navigating to DealerActivity for dealer user.")
                val intent = Intent(requireContext(), DealerActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Optional: Prevent returning to logi
            } else {
                findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onLoginSuccess() {
        listener?.onLoginSuccess()
    }
} 