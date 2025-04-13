//package com.example.dnmotors.di
//
//import AuthViewModel
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.dsl.module
//import com.example.domain.usecase.SignInWithEmailUseCase
//import com.example.domain.usecase.SignInWithGoogleUseCase
//import com.example.domain.usecase.CheckUserSignedInUseCase
//
//val appModule = module {
//    viewModel {
//        AuthViewModel(
//            signInWithEmail = get(),
//            signInWithGoogle = get(),
//            checkUserSignedIn = get()
//        )
//    }
//}
