package com.mool.feature.dashboard

import com.mool.core.domain.ExchangeRate

data class DashboardState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val preferredCurrency: String = "USD",
    val rates: List<ExchangeRate> = emptyList(),
    val error: String? = null,
)
