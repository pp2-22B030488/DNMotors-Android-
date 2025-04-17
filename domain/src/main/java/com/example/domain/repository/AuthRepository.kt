package com.example.domain.repository

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun registerWithGoogle(idToken: String): Result<Unit>
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<Unit>
    fun isUserSignedIn(): Boolean
}