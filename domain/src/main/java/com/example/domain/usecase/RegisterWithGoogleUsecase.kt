package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class RegisterWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.registerWithGoogle(idToken)
    }
}