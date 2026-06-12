package com.mool.feature.settings

import com.mool.core.ui.ThemeMode

sealed interface SettingsIntent {
    data class SetCurrency(val currency: String) : SettingsIntent
    data class SetBiometricLock(val enabled: Boolean) : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data class SetBudget(val category: String, val amount: String) : SettingsIntent
    data object LoadSettings : SettingsIntent
}
