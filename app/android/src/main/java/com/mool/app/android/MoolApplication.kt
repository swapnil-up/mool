package com.mool.app.android

import android.app.Application
import com.mool.di.AppModule
import com.mool.di.initKoin
import org.koin.core.annotation.KoinApplication

@KoinApplication(modules = [AppModule::class])
class MoolApp

class MoolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
