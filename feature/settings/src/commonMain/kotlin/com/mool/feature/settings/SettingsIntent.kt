package com.mool.feature.settings

sealed interface SettingsIntent {
    data class SetCurrency(val currency: String) : SettingsIntent
    data object LoadSettings : SettingsIntent
}
