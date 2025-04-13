package com.example.dnmotors.di

import com.example.dnmotors.viewmodel.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        AuthViewModel(
            signInWithEmail = get(),
            signInWithGoogle1 = get(),
            checkUserSignedIn = get()
        )
    }
}
