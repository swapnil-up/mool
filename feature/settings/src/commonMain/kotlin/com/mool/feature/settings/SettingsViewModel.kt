package com.mool.feature.settings

import com.mool.core.domain.SettingsKeys
import com.mool.core.domain.repository.SettingsRepository
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

    fun accept(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> observePreferences()
            is SettingsIntent.SetCurrency -> setCurrency(intent.currency)
            is SettingsIntent.SetBiometricLock -> setBiometricLock(intent.enabled)
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
}
