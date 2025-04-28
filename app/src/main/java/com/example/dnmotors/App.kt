package com.example.dnmotors

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.dnmotors.services.MessageService
import com.example.dnmotors.di.appModules
import com.example.dnmotors.di.viewModelModule
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    companion object {
        lateinit var instance: App
            private set

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (FirebaseAuth.getInstance().currentUser != null) {
            val serviceIntent = Intent(this, MessageService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(appModules)
            modules(listOf(viewModelModule))
        }
    }
}
