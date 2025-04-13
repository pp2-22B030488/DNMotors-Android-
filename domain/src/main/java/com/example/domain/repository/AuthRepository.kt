package com.example.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean>
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<Boolean>
    fun isUserSignedIn(): Boolean
}
