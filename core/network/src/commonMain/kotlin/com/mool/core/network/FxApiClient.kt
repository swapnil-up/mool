package com.mool.core.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class FxApiClient(private val client: HttpClient) {

    suspend fun fetchRates(baseCurrency: String): String {
        return client.get("https://open.er-api.com/v6/latest/$baseCurrency").bodyAsText()
    }
}
