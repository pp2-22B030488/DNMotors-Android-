package com.example.dnmotors.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.CheckUserSignedInUseCase
import com.example.domain.usecase.SignInWithEmailUseCase
import com.example.domain.usecase.SignInWithGoogleUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signInWithGoogle1: SignInWithGoogleUseCase,
    private val checkUserSignedIn: CheckUserSignedInUseCase
) : ViewModel() {

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = signInWithEmail(email, password)
            _authState.value = result.isSuccess
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val result = signInWithGoogle1(idToken)
            _authState.value = result.isSuccess
        }
    }


    fun isUserSignedIn(): Boolean = checkUserSignedIn()
}
