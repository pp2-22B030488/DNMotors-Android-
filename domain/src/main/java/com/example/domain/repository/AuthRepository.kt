package com.example.domain.repository

import com.example.domain.model.AuthUser

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        location: String,
        phoneNumber: String
    ): Result<Unit>
    suspend fun registerWithGoogle(idToken: String): Result<Unit>
    suspend fun fetchUserRole(uid: String): Result<String>
    fun isUserSignedIn(): Boolean
    suspend fun clearChatListeners()
    suspend fun setupFirestorePersistence()
    suspend fun returnAuth(): AuthUser
}
