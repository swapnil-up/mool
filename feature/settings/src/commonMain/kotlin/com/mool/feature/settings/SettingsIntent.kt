package com.mool.feature.settings

import com.mool.core.ui.ThemeMode

sealed interface SettingsIntent {
    data class SetCurrency(val currency: String) : SettingsIntent
    data class SetBiometricLock(val enabled: Boolean) : SettingsIntent
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
    data object LoadSettings : SettingsIntent
}
