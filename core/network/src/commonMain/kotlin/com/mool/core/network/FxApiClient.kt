package com.mool.core.network

import com.mool.core.network.model.FxRateResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FxApiClient(private val client: HttpClient) {

    suspend fun fetchRates(baseCurrency: String): FxRateResponse {
        return client.get("https://open.er-api.com/v6/latest/$baseCurrency").body()
    }
}
