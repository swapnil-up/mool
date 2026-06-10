package com.mool.core.domain

data class ExchangeRate(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val timestamp: Long,
)
