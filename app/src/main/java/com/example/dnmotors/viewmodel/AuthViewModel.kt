package com.example.dnmotors.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.CheckUserSignedInUseCase
import com.example.domain.usecase.RegisterWithEmailUseCase
import com.example.domain.usecase.RegisterWithGoogleUseCase
import com.example.domain.usecase.SignInWithEmailUseCase
import com.example.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val registerWithGoogleUseCase: RegisterWithGoogleUseCase,
    private val checkUserSignedInUseCase: CheckUserSignedInUseCase
) : ViewModel() {

    private val _authState = MutableLiveData<AuthResult>()
    val authState: LiveData<AuthResult> = _authState

    private val TAG = "AuthViewModel"

    fun signIn(email: String, password: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            Log.d(TAG, "Executing SignInWithEmailUseCase for $email")
            val result = signInWithEmailUseCase(email, password)
            result.onSuccess {
                Log.i(TAG, "Sign in success for $email")
                _authState.value = AuthResult.Success
            }.onFailure { error ->
                Log.e(TAG, "Sign in failure for $email: ${error.message}", error)
                _authState.value = AuthResult.Error(error.message ?: "Sign-in failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            Log.d(TAG, "Executing SignInWithGoogleUseCase")
            val result = signInWithGoogleUseCase(idToken)
            result.onSuccess {
                Log.i(TAG, "Google Sign in success")
                _authState.value = AuthResult.Success
            }.onFailure { error ->
                Log.e(TAG, "Google Sign in failure: ${error.message}", error)
                _authState.value = AuthResult.Error(error.message ?: "Google Sign-in failed")
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            Log.d(TAG, "Executing RegisterWithEmailUseCase for $email, Name: $name")
            val result = registerWithEmailUseCase(email, password, name)
            result.onSuccess {
                Log.i(TAG, "Registration success for $email")
                _authState.value = AuthResult.Success
            }.onFailure { error ->
                Log.e(TAG, "Registration failure for $email: ${error.message}", error)
                _authState.value = AuthResult.Error(error.message ?: "Registration failed")
            }
        }
    }
    fun registerWithGoogle(idToken: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            Log.d(TAG, "Executing RegisterWithGoogleUseCase")
            val result = registerWithGoogleUseCase(idToken)
            result.onSuccess {
                Log.i(TAG, "Google Registration/Sign-in success")
                _authState.value = AuthResult.Success
            }.onFailure { error ->
                Log.e(TAG, "Google Registration/Sign-in failure: ${error.message}", error)
                _authState.value = AuthResult.Error(error.message ?: "Google Registration/Sign-in failed")
            }
        }
    }
    fun isUserSignedIn(): Boolean {
        val signedIn = checkUserSignedInUseCase()
        Log.d(TAG, "Checked if user signed in via UseCase: $signedIn")
        return signedIn
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    // object Idle : AuthResult() // Optional: Initial state before any action
}