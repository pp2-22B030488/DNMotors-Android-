package com.example.dnmotors.di

import com.example.data.repository.AuthRepositoryImpl
import com.example.dnmotors.viewmodel.AuthViewModel
import com.example.domain.repository.AuthRepository
import com.example.domain.usecase.AuthUseCase
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {

    single { Firebase.auth }
    single { FirebaseFirestore.getInstance() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    single { AuthUseCase(get()) }

    viewModel { AuthViewModel(get()) }
}
