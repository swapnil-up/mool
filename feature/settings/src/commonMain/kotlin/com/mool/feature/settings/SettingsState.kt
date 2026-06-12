package com.mool.feature.settings

import com.mool.core.ui.ThemeMode

data class SettingsState(
    val preferredCurrency: String = "USD",
    val biometricEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val isLoading: Boolean = true,
    val error: String? = null,
    val budgets: Map<String, String> = emptyMap(),
)
