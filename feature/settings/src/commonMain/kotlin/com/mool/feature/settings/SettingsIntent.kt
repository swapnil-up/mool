package com.mool.feature.settings

sealed interface SettingsIntent {
    data class SetCurrency(val currency: String) : SettingsIntent
    data class SetBiometricLock(val enabled: Boolean) : SettingsIntent
    data object LoadSettings : SettingsIntent
}
