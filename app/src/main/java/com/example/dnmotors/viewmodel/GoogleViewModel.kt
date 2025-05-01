package com.example.dnmotors.viewmodel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class GoogleViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _googleSignInState = MutableLiveData<AuthResult>()
    val googleSignInState: LiveData<AuthResult> get() = _googleSignInState

    fun handleGoogleSignInResult(intent: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)

            if (account?.idToken != null) {
                _googleSignInState.value = AuthResult.Loading
                authViewModel.signInWithGoogle(account.idToken!!)
            } else {
                _googleSignInState.value = AuthResult.Error("Google Sign-In failed: ID token is null.")
            }
        } catch (e: ApiException) {
            _googleSignInState.value = AuthResult.Error("Google Sign-In failed with ApiException: ${e.statusCode}")
        } catch (e: Exception) {
            _googleSignInState.value = AuthResult.Error("Google Sign-In failed with unexpected exception: ${e.localizedMessage}")
        }
    }

    fun handleGoogleRegistrationResult(intent: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)

            if (account?.idToken != null) {
                _googleSignInState.value = AuthResult.Loading
                authViewModel.registerWithGoogle(account.idToken!!)
            } else {
                _googleSignInState.value = AuthResult.Error("Google Registration failed: ID token is null.")
            }
        } catch (e: ApiException) {
            _googleSignInState.value = AuthResult.Error("Google Registration failed with ApiException: ${e.statusCode}")
        } catch (e: Exception) {
            _googleSignInState.value = AuthResult.Error("Google Registration failed with unexpected exception: ${e.localizedMessage}")
        }
    }
}
