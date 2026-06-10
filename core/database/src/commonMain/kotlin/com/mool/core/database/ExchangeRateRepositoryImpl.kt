package com.mool.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mool.core.domain.ExchangeRate
import com.mool.core.domain.repository.ExchangeRateRepository
import com.mool.core.network.FxApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExchangeRateRepositoryImpl(
    private val apiClient: FxApiClient,
    db: MoolDatabase? = null,
) : ExchangeRateRepository {

    private val queries = db?.exchangeRateQueries

    override fun observeRates(): Flow<List<ExchangeRate>> {
        if (queries == null) return emptyFlow()
        return queries.getAllRates()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshRates() {
        val response = apiClient.fetchRates("USD")
        if (queries != null) {
            withContext(Dispatchers.Default) {
                queries.transaction {
                    response.rates.forEach { (currency, rate) ->
                        queries.upsertRate(
                            from_currency = "USD",
                            to_currency = currency,
                            rate = rate,
                            updated_at = response.timeLastUpdateUnix,
                        )
                    }
                }
            }
        }
    }

    private fun Exchange_rates.toDomain() = ExchangeRate(
        fromCurrency = from_currency,
        toCurrency = to_currency,
        rate = rate,
        timestamp = updated_at,
    )
}
