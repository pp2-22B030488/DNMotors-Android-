package com.example.dnmotors.di

import com.example.data.repository.ChatRepositoryImpl
import com.example.dnmotors.viewmodel.ChatViewModel
import com.example.domain.repository.ChatRepository
import com.example.domain.usecase.ChatUseCases
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single<ChatRepository> { ChatRepositoryImpl() }

    single { ChatUseCases(get()) }

    viewModel { ChatViewModel(get()) }
}
