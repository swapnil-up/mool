package com.mool.feature.settings

import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.repository.SettingsRepository
import com.mool.core.ui.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val isBiometricSupported: Boolean = false,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    private val budgetCategories = listOf("Food", "Transport", "Shopping", "Bills")

    fun accept(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observePreferences()
            is SettingsIntent.SetCurrency -> setCurrency(intent.currency)
            is SettingsIntent.SetBiometricLock -> setBiometricLock(intent.enabled)
            is SettingsIntent.SetThemeMode -> setThemeMode(intent.mode)
            is SettingsIntent.SetBudget -> setBudget(intent.category, intent.amount)
        }
    }

    fun observePreferences() {
        observeJob?.cancel()
        observeJob = scope.launch {
            settingsRepository.observeSetting(SettingsKeys.PREFERRED_CURRENCY).collect { value ->
                _state.update { it.copy(preferredCurrency = value ?: "USD") }
            }
        }
        scope.launch {
            settingsRepository.observeSetting(SettingsKeys.BIOMETRIC_ENABLED).collect { value ->
                _state.update { it.copy(biometricEnabled = value == "true", isBiometricAvailable = isBiometricSupported) }
            }
        }
        scope.launch {
            settingsRepository.observeSetting(SettingsKeys.THEME_MODE).collect { value ->
                val mode = when (value) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    else -> ThemeMode.FOLLOW_SYSTEM
                }
                _state.update { it.copy(themeMode = mode) }
            }
        }
        budgetCategories.forEach { category ->
            scope.launch {
                val key = "budget_${category.lowercase()}"
                settingsRepository.observeSetting(key).collect { value ->
                    _state.update { s ->
                        s.copy(budgets = s.budgets + (category to (value ?: "")))
                    }
                }
            }
        }
        scope.launch {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun setCurrency(currency: String) {
        scope.launch {
            try {
                settingsRepository.setSetting(SettingsKeys.PREFERRED_CURRENCY, currency)
            } catch (e: Exception) {
                _effects.send(SettingsEffect.ShowError(e.message ?: "Failed to save preference"))
            }
        }
    }

    private fun setBiometricLock(enabled: Boolean) {
        scope.launch {
            try {
                settingsRepository.setSetting(SettingsKeys.BIOMETRIC_ENABLED, enabled.toString())
            } catch (e: Exception) {
                _effects.send(SettingsEffect.ShowError(e.message ?: "Failed to update biometric setting"))
            }
        }
    }

    private fun setThemeMode(mode: ThemeMode) {
        scope.launch {
            try {
                val value = when (mode) {
                    ThemeMode.LIGHT -> "light"
                    ThemeMode.DARK -> "dark"
                    ThemeMode.FOLLOW_SYSTEM -> "system"
                }
                settingsRepository.setSetting(SettingsKeys.THEME_MODE, value)
                _state.update { it.copy(themeMode = mode) }
            } catch (e: Exception) {
                _effects.send(SettingsEffect.ShowError(e.message ?: "Failed to update theme"))
            }
        }
    }

    private fun setBudget(category: String, amount: String) {
        scope.launch {
            try {
                val key = "budget_${category.lowercase()}"
                if (amount.isBlank()) {
                    settingsRepository.setSetting(key, "")
                } else {
                    val parsed = amount.toDoubleOrNull()
                    if (parsed != null && parsed >= 0) {
                        settingsRepository.setSetting(key, amount)
                    }
                }
            } catch (e: Exception) {
                _effects.send(SettingsEffect.ShowError(e.message ?: "Failed to save budget"))
            }
        }
    }
}
