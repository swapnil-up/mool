package com.mool.core.database.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mool.core.database.MoolDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule(): org.koin.core.module.Module = module {
    single<SqlDriver> {
        val context = androidContext()
        AndroidSqliteDriver(MoolDatabase.Schema, context, "mool.db")
    }
    single { MoolDatabase(get()) }
}
