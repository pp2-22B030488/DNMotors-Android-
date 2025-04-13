package com.example.domain.usecase

import com.example.domain.repository.AuthRepository

class SignInWithEmailUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repo.signInWithEmail(email, password)
    }
}

class SignInWithGoogleUseCase(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Boolean> {
        return repo.signInWithGoogle(idToken)
    }
}


class CheckUserSignedInUseCase(private val repo: AuthRepository) {
    operator fun invoke() = repo.isUserSignedIn()
}
