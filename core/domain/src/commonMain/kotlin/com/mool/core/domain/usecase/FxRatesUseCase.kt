package com.mool.core.domain.usecase

import com.mool.core.domain.ExchangeRate
import com.mool.core.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow

class FxRatesUseCase(private val repository: ExchangeRateRepository) {

    fun observeRates(): Flow<List<ExchangeRate>> = repository.observeRates()

    suspend fun refreshRates() = repository.refreshRates()
}
