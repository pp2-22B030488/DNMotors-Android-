package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class CheckUserSignedInUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Boolean {
        return authRepository.isUserSignedIn()
    }
}