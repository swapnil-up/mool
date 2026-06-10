package com.mool.feature.settings

sealed interface SettingsEffect {
    data class ShowError(val message: String) : SettingsEffect
}
