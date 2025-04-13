package com.example.dnmotors.di

import com.example.domain.repository.AuthRepository
import com.example.domain.repository.FirebaseAuthRepositoryImpl
import com.example.domain.usecase.SignInWithEmailUseCase
import com.example.domain.usecase.SignInWithGoogleUseCase
import com.example.domain.usecase.CheckUserSignedInUseCase
import com.google.firebase.auth.FirebaseAuth
import org.koin.dsl.module

val domainModule = module {
    single { FirebaseAuth.getInstance() }
    single { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    single<AuthRepository> { FirebaseAuthRepositoryImpl(get(), get()) }
    factory { SignInWithEmailUseCase(get()) }
    factory { SignInWithGoogleUseCase(get()) }
    factory { CheckUserSignedInUseCase(get()) }
}
