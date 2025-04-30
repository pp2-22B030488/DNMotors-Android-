package com.example.dnmotors.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.AuthUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthUseCase
) : ViewModel() {

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    fun isUserSignedIn(): Boolean {
        return authRepository.isUserSignedIn()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = authRepository.signInWithEmail(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthResult.Success },
                onFailure = { AuthResult.Error(it.message ?: "Sign in failed") }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = authRepository.signInWithGoogle(idToken)
            _authState.value = result.fold(
                onSuccess = { AuthResult.Success },
                onFailure = { AuthResult.Error(it.message ?: "Google sign in failed") }
            )
        }
    }

    fun register(email: String, password: String, name: String, location: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = authRepository.registerWithEmail(email, password, name, location, phoneNumber)
            _authState.value = result.fold(
                onSuccess = { AuthResult.Success },
                onFailure = { AuthResult.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun registerWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = authRepository.registerWithGoogle(idToken)
            _authState.value = result.fold(
                onSuccess = { AuthResult.Success },
                onFailure = { AuthResult.Error(it.message ?: "Google registration failed") }
            )
        }
    }

    fun fetchUserRole(callback: (String) -> Unit) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val result = authRepository.fetchUserRole(userId)
                callback(result.getOrDefault("user"))
            } else {
                callback("user")
            }
        }
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}