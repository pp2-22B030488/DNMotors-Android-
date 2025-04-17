package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class RegisterWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be blank"))
        }
        return authRepository.registerWithEmail(email, password, name)
    }
}