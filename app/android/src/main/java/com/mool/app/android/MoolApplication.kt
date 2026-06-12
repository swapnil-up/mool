package com.mool.app.android

import android.app.Application
import com.mool.core.database.di.platformModule
import com.mool.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.core.context.startKoin

@KoinApplication(modules = [AppModule::class])
class MoolApp

class MoolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoolApplication)
            modules(platformModule())
        }
    }
}
