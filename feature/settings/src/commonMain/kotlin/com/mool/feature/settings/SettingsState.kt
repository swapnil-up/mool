package com.mool.feature.settings

data class SettingsState(
    val preferredCurrency: String = "USD",
    val isLoading: Boolean = true,
    val error: String? = null,
)
