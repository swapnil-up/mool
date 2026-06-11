package com.mool.core.database.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mool.core.database.MoolDatabase
import org.koin.dsl.module

actual fun platformModule(): org.koin.core.module.Module = module {
    single<SqlDriver> { NativeSqliteDriver(MoolDatabase.Schema, "mool.db") }
    single { MoolDatabase(get()) }
}
