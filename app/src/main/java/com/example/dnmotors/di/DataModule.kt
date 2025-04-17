package com.example.dnmotors.di

import com.example.data.repository.FirebaseAuthRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single<AuthRepository> { FirebaseAuthRepositoryImpl(firebaseAuth = get(), firestore = get()) }
}