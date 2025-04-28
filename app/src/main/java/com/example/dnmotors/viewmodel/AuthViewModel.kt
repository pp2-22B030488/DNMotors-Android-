package com.example.dnmotors.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnmotors.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthViewModel"

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthResult.Success
                Log.d(TAG, "Sign in successful for user: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "Sign in failed", e)
                _authState.value = AuthResult.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _authState.value = AuthResult.Success
                Log.d(TAG, "Google sign in successful for user: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "Google sign in failed", e)
                _authState.value = AuthResult.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun register(email: String, password: String, name: String, location: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Update user profile
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(android.net.Uri.parse("https://example.com/default_profile_image.jpg"))
                        .build()
                )?.await()

                // Create user document in Firestore
                result.user?.let { user ->
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "location" to location,
                        "phoneNumber" to phoneNumber,
                        "role" to "user"
                    )
                    firestore.collection("users").document(user.uid).set(userData).await()
                }

                _authState.value = AuthResult.Success
                Log.d(TAG, "Registration successful for user: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
                _authState.value = AuthResult.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun registerWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                
                // Create user document in Firestore if it doesn't exist
                result.user?.let { user ->
                    val userDoc = firestore.collection("users").document(user.uid).get().await()
                    if (!userDoc.exists()) {
                        val userData = hashMapOf(
                            "name" to user.displayName,
                            "email" to user.email,
                            "role" to "user"
                        )
                        firestore.collection("users").document(user.uid).set(userData).await()
                    }
                }

                _authState.value = AuthResult.Success
                Log.d(TAG, "Google registration successful for user: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "Google registration failed", e)
                _authState.value = AuthResult.Error(e.message ?: "Google registration failed")
            }
        }
    }

    fun fetchUserRole(callback: (String) -> Unit) {
        auth.currentUser?.let { user ->
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role") ?: "user"
                    callback(role)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching user role", e)
                    callback("user")
                }
        } ?: run {
            callback("user")
        }
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}