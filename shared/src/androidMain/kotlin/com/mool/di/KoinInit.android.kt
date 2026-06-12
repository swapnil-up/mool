package com.mool.di

import android.content.Context
import com.mool.core.database.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(platformModule(), AppModule().module())
    }
}
