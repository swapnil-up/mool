package com.mool.app.android

import android.app.Application
import com.mool.core.database.di.platformModule
import com.mool.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MoolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoolApplication)
            modules(appModule, platformModule())
        }
    }
}
