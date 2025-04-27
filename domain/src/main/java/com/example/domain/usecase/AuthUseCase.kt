package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class SignInWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.signInWithGoogle(idToken)
    }
}
class RegisterWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.registerWithGoogle(idToken)
    }
}
class CheckUserSignedInUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Boolean {
        return authRepository.isUserSignedIn()
    }
}
class RegisterWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be blank"))
        }
        return authRepository.registerWithEmail(email, password, name)
    }
}

class SignInWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.signInWithEmail(email, password)
    }
}