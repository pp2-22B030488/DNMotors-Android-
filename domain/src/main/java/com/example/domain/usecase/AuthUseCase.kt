package com.example.domain.usecase

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository

class AuthUseCase(private val repository: AuthRepository) {

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return repository.signInWithEmail(email, password)
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return repository.signInWithGoogle(idToken)
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        location: String,
        phoneNumber: String
    ): Result<Unit> {
        return repository.registerWithEmail(email, password, name, location, phoneNumber)
    }

    suspend fun registerWithGoogle(idToken: String): Result<Unit> {
        return repository.registerWithGoogle(idToken)
    }

    suspend fun fetchUserRole(uid: String): Result<String> {
        return repository.fetchUserRole(uid)
    }

    fun isUserSignedIn(): Boolean {
        return repository.isUserSignedIn()
    }

    suspend fun clearChatListeners() {
        return repository.clearChatListeners()
    }

    suspend fun setupFirestorePersistence(){
        return repository.setupFirestorePersistence()
    }

    suspend fun returnAuth(): AuthUser {
        return repository.returnAuth()
    }
}
