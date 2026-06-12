package com.mool.di

import com.mool.core.data.ExchangeRateRepositoryImpl
import com.mool.core.database.MoolDatabase
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
import com.mool.feature.dashboard.DashboardViewModel
import com.mool.feature.remittance.RemittanceViewModel
import com.mool.feature.transactions.TransactionFormViewModel
import com.mool.feature.transactions.TransactionHistoryViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
class AppModule {
    @Single
    fun provideHttpClient(): io.ktor.client.HttpClient = MoolHttpClient.create()

    @Single
    fun provideFxApiClient(client: io.ktor.client.HttpClient): FxApiClient = FxApiClient(client)

    @Single
    fun provideEncryptionManager(): EncryptionManager = EncryptionManager()

    @Single
    fun provideClock(): Clock = SystemClock()

    @Single
    fun provideSettingsRepository(@Provided db: MoolDatabase): SettingsRepository =
        SettingsRepositoryImpl(db)

    @Single
    fun provideTransactionRepository(
        @Provided db: MoolDatabase,
        encryptionManager: EncryptionManager,
    ): TransactionRepository = TransactionRepositoryImpl(db, encryptionManager)

    @Single
    fun provideExchangeRateRepository(
        apiClient: FxApiClient,
        @Provided db: MoolDatabase,
    ): ExchangeRateRepository = ExchangeRateRepositoryImpl(apiClient, db)

    @Single
    fun provideTransactionHistoryViewModel(
        repository: TransactionRepository,
    ): TransactionHistoryViewModel = TransactionHistoryViewModel(repository)

    @Single
    fun provideRemittanceViewModel(
        exchangeRateRepository: ExchangeRateRepository,
    ): RemittanceViewModel = RemittanceViewModel(exchangeRateRepository)

    @Single
    fun provideDashboardViewModel(
        exchangeRateRepository: ExchangeRateRepository,
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository,
    ): DashboardViewModel = DashboardViewModel(
        exchangeRateRepository,
        transactionRepository,
        settingsRepository,
    )

    @Single
    fun provideTransactionFormViewModel(
        repository: TransactionRepository,
        clock: Clock,
    ): TransactionFormViewModel = TransactionFormViewModel(repository, clock)
}
