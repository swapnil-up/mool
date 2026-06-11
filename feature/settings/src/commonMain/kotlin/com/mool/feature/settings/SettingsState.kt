package com.mool.feature.settings

data class SettingsState(
    val preferredCurrency: String = "USD",
    val biometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)
