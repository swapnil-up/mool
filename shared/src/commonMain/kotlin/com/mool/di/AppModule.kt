package com.mool.di

import com.mool.core.data.ExchangeRateRepositoryImpl
import com.mool.core.database.SettingsRepositoryImpl
import com.mool.core.database.SystemClock
import com.mool.core.database.TransactionRepositoryImpl
import com.mool.core.domain.clock.Clock
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.domain.repository.TransactionRepository
import com.mool.core.network.FxApiClient
import com.mool.core.network.MoolHttpClient
import com.mool.core.security.EncryptionManager
import org.koin.dsl.module

val appModule = module {
    single { MoolHttpClient.create() }
    single { FxApiClient(get()) }
    single { EncryptionManager() }
    single<Clock> { SystemClock() }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
    single<ExchangeRateRepository> { ExchangeRateRepositoryImpl(get(), get()) }
}
