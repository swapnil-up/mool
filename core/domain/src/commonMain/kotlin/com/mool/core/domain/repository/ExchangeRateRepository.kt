package com.mool.core.domain.repository

import com.mool.core.domain.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {
    fun observeRates(): Flow<List<ExchangeRate>>
    suspend fun refreshRates()
}
