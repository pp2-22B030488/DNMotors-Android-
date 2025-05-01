package com.example.dnmotors.di

import com.example.dnmotors.viewmodel.GoogleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val googleModule = module {
    viewModel { GoogleViewModel(get()) }
}
