package com.example.dnmotors.di

import com.example.data.repository.ChatRepositoryImpl
import com.example.data.source.AppDatabase
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.repository.ChatRepository
import com.example.domain.usecase.ChatUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
//    single { AppDatabase.getDatabase(androidContext()) }
//    single { get<AppDatabase>().chatItemDao() }
//    single { get<AppDatabase>().messageDao() }
//    single { CoroutineScope(SupervisorJob()) }
//
//    single<ChatRepository> {
//        ChatRepositoryImpl(get(), get(), get())
//    }
    single<ChatRepository> { ChatRepositoryImpl() }

    single { ChatUseCases(get()) }

    viewModel { ChatViewModel(get()) }
}
