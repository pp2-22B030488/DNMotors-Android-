package com.example.dnmotors.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AuthUser
import com.example.domain.usecase.AuthUseCase
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
            val userId = authRepository.returnAuth()
            if (userId.uid != null) {
                val result = authRepository.fetchUserRole(userId.uid!!)
                callback(result.getOrDefault("user"))
            } else {
                callback("user")
            }
        }
    }

    fun clearChatListeners() {
        viewModelScope.launch {
            return@launch authRepository.clearChatListeners()
        }
    }

    fun setupFirestorePersistence(){
        viewModelScope.launch {
            return@launch authRepository.setupFirestorePersistence()
        }
    }

    suspend fun returnAuth(): AuthUser {
        return authRepository.returnAuth()
    }

    fun fetchAuthInfo(callback: (AuthUser) -> Unit) {
        viewModelScope.launch {
            val authUser = authRepository.returnAuth()
            callback(authUser)
        }
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}