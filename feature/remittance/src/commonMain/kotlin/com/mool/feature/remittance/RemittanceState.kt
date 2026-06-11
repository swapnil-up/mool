package com.mool.feature.remittance

data class RemittanceState(
    val sendAmount: String = "",
    val sendCurrency: String = "USD",
    val receiveCurrency: String = "NPR",
    val feePercent: String = "1.0",
    val marginPercent: String = "0.5",
    val calculatedReceiveAmount: Double? = null,
    val calculatedFee: Double? = null,
    val calculatedMargin: Double? = null,
    val appliedRate: Double? = null,
    val availableCurrencies: List<String> = emptyList(),
    val isRatesLoading: Boolean = false,
    val isCalculating: Boolean = false,
    val error: String? = null,
)
