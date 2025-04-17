package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class SignInWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.signInWithEmail(email, password)
    }
}