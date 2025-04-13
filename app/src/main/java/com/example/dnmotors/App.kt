package com.example.dnmotors

import android.app.Application
import com.example.dnmotors.di.domainModule
import com.example.dnmotors.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                domainModule,
                viewModelModule
            )
        }
    }
}
