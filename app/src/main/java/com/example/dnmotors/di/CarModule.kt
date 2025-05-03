//package com.example.dnmotors.di
//
//import androidx.room.Room
//import com.example.data.repository.CarRepository
//import com.example.data.source.AppDatabase
//import com.example.dnmotors.App
//import com.example.dnmotors.viewmodel.CarViewModel
//import org.koin.core.module.dsl.viewModel
//import org.koin.dsl.module
//
//val carModule = module {
//
//    single { App.database }
//
//
//    single { get<AppDatabase>().carDao() }
//
//    single { CarRepository(get()) }
//
//    viewModel { CarViewModel(get()) }
//
//}
